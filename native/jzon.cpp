/*
Copyright (c) 2012 Johannes Häggqvist

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
#include "jzon.h"

#include <sstream>
#include <fstream>
#include <algorithm>
#include <stack>

namespace Jzon
{
	class FormatInterpreter
	{
	public:
		FormatInterpreter()
		{
			SetFormat(NoFormat);
		}
		FormatInterpreter(const Format &format)
		{
			SetFormat(format);
		}

		void SetFormat(const Format &format)
		{
			this->format = format;
			indentationChar = (format.useTabs ? '\t' : ' ');
			spacing = (format.spacing ? " " : "");
			newline = (format.newline ? "\n" : spacing);
		}

		std::string GetIndentation(unsigned int level) const
		{
			if (!format.newline)
			{
				return "";
			}
			else
			{
				return std::string(format.indentSize * level, indentationChar);
			}
		}
		
		inline const std::string &GetNewline() const
		{
			return newline;
		}
		inline const std::string &GetSpacing() const
		{
			return spacing;
		}

	private:
		Format format;
		char indentationChar;
		std::string newline;
		std::string spacing;
	};

	void RemoveWhitespace(const std::string &json, std::string &freshJson)
	{
		freshJson.clear();

		bool comment = false;
		int multicomment = 0;
		bool inString = false;

		for (std::string::const_iterator it = json.begin(); it != json.end(); ++it)
		{
			char c0 = '\0';
			char c1 = (*it);
			char c2 = '\0';
			if (it != json.begin())
				c0 = (*(it-1));
			if (it+1 != json.end())
				c2 = (*(it+1));

			if (c0 != '\\' && c1 == '"')
			{
				inString = !inString;
			}

			if (!inString)
			{
				if (c1 == '/' && c2 == '*')
				{
					++multicomment;
					if (it+1 != json.end())
						++it;
					continue;
				}
				else if (c1 == '*' && c2 == '/')
				{
					--multicomment;
					if (it+1 != json.end())
						++it;
					continue;
				}
				else if (c1 == '/' && c2 == '/')
				{
					comment = true;
					if (it+1 != json.end())
						++it;
					continue;
				}
				else if (c1 == '\n')
				{
					comment = false;
				}
			}

			if (comment || multicomment > 0)
				continue;

			if (inString)
			{
				freshJson += c1;
			}
			else
			{
				if ((c1 != '\n')&&(c1 != ' ')&&(c1 != '\t')&&(c1 != '\r')&&(c1 != '\f'))
					freshJson += c1;
			}
		}
	}

	Node::Node()
	{
	}
	Node::~Node()
	{
	}

	Object &Node::AsObject()
	{
		if (IsObject())
			return static_cast<Object&>(*this);
		else
			throw TypeException();
	}
	const Object &Node::AsObject() const
	{
		if (IsObject())
			return static_cast<const Object&>(*this);
		else
			throw TypeException();
	}
	Array &Node::AsArray()
	{
		if (IsArray())
			return static_cast<Array&>(*this);
		else
			throw TypeException();
	}
	const Array &Node::AsArray() const
	{
		if (IsArray())
			return static_cast<const Array&>(*this);
		else
			throw TypeException();
	}
	Value &Node::AsValue()
	{
		if (IsValue())
			return static_cast<Value&>(*this);
		else
			throw TypeException();
	}
	const Value &Node::AsValue() const
	{
		if (IsValue())
			return static_cast<const Value&>(*this);
		else
			throw TypeException();
	}

	Node::Type Node::DetermineType(const std::string &_json)
	{
		std::string json;
		RemoveWhitespace(_json, json);

		Node::Type type;
		switch (json.at(0))
		{
		case '{' : type = T_OBJECT; break;
		case '[' : type = T_ARRAY; break;
		default : type = T_VALUE; break;
		}

		return type;
	}


	Value::Value()
	{
		SetNull();
	}
	Value::Value(const Value &rhs)
	{
		Set(rhs);
	}
	Value::Value(const Node &rhs)
	{
		const Value &value = rhs.AsValue();
		Set(value);
	}
	Value::Value(ValueType type, const std::string &value)
	{
		Set(type, value);
	}
	Value::Value(const std::string &value)
	{
		Set(value);
	}
	Value::Value(const char *value)
	{
		Set(value);
	}
	Value::Value(const int value)
	{
		Set(value);
	}
	Value::Value(const float value)
	{
		Set(value);
	}
	Value::Value(const double value)
	{
		Set(value);
	}
	Value::Value(const bool value)
	{
		Set(value);
	}
	Value::~Value()
	{
	}

	Node::Type Value::GetType() const
	{
		return T_VALUE;
	}
	Value::ValueType Value::GetValueType() const
	{
		return type;
	}

	std::string Value::ToString() const
	{
		if (IsNull())
			return "null";
		else
			return valueStr;
	}
	int Value::ToInt() const
	{
		if (IsNull())
			return 0;
		else
		{
			if (IsNumber())
			{
				std::stringstream sstr(valueStr);
				int val;
				sstr >> val;
				return val;
			}
			else
				throw ValueException();
		}
	}
	float Value::ToFloat() const
	{
		if (IsNull())
			return 0.0;
		else
		{
			if (IsNumber())
			{
				std::stringstream sstr(valueStr);
				float val;
				sstr >> val;
				return val;
			}
			else
				throw ValueException();
		}
	}
	double Value::ToDouble() const
	{
		if (IsNull())
			return 0.0;
		else
		{
			if (IsNumber())
			{
				std::stringstream sstr(valueStr);
				double val;
				sstr >> val;
				return val;
			}
			else
				throw ValueException();
		}
	}
	bool Value::ToBool() const
	{
		if (IsNull())
			return false;
		else
			if (IsBool())
				return (valueStr == "true");
			else
				throw ValueException();
	}

	void Value::SetNull()
	{
		valueStr = "";
		type     = VT_NULL;
	}
	void Value::Set(const Value &value)
	{
		if (this != &value)
		{
			valueStr = value.valueStr;
			type     = value.type;
		}
	}
	void Value::Set(ValueType type, const std::string &value)
	{
		valueStr   = value;
		this->type = type;
	}
	void Value::Set(const std::string &value)
	{
		valueStr = UnescapeString(value);
		type     = VT_STRING;
	}
	void Value::Set(const char *value)
	{
		valueStr = UnescapeString(std::string(value));
		type     = VT_STRING;
	}
	void Value::Set(const int value)
	{
		std::stringstream sstr;
		sstr << value;
		valueStr = sstr.str();
		type     = VT_NUMBER;
	}
	void Value::Set(const float value)
	{
		std::stringstream sstr;
		sstr << value;
		valueStr = sstr.str();
		type     = VT_NUMBER;
	}
	void Value::Set(const double value)
	{
		std::stringstream sstr;
		sstr << value;
		valueStr = sstr.str();
		type     = VT_NUMBER;
	}
	void Value::Set(const bool value)
	{
		if (value)
			valueStr = "true";
		else
			valueStr = "false";
		type = VT_BOOL;
	}

	Value &Value::operator=(const Value &rhs)
	{
		if (this != &rhs)
			Set(rhs);
		return *this;
	}
	Value &Value::operator=(const Node &rhs)
	{
		if (this != &rhs)
			Set(rhs.AsValue());
		return *this;
	}
	Value &Value::operator=(const std::string &rhs)
	{
		Set(rhs);
		return *this;
	}
	Value &Value::operator=(const char *rhs)
	{
		Set(rhs);
		return *this;
	}
	Value &Value::operator=(const int rhs)
	{
		Set(rhs);
		return *this;
	}
	Value &Value::operator=(const float rhs)
	{
		Set(rhs);
		return *this;
	}
	Value &Value::operator=(const double rhs)
	{
		Set(rhs);
		return *this;
	}
	Value &Value::operator=(const bool rhs)
	{
		Set(rhs);
		return *this;
	}

	bool Value::operator==(const Value &other) const
	{
		return ((type == other.type)&&(valueStr == other.valueStr));
	}
	bool Value::operator!=(const Value &other) const
	{
		return !(*this == other);
	}

	Node *Value::GetCopy() const
	{
		return new Value(*this);
	}

	// This is not the most beautiful place for these, but it'll do
	static const char charsUnescaped[] = { '\\'  , '\"'  , '\n' , '\t' , '\b' , '\f' , '\r' };
	static const char *charsEscaped[]  = { "\\\\", "\\\"", "\\n", "\\t", "\\b", "\\f", "\\r" };
	static const unsigned int numEscapeChars = 7;
	static const char nullUnescaped = '\0';
	static const char *nullEscaped  = "\0\0";
	const char *&getEscaped(const char &c)
	{
		for (unsigned int i = 0; i < numEscapeChars; ++i)
		{
			const char &ue = charsUnescaped[i];

			if (c == ue)
			{
				const char *&e = charsEscaped[i];
				return e;
			}
		}
		return nullEscaped;
	}
	const char &getUnescaped(const char &c1, const char &c2)
	{
		for (unsigned int i = 0; i < numEscapeChars; ++i)
		{
			const char *&e = charsEscaped[i];

			if (c1 == e[0] && c2 == e[1])
			{
				const char &ue = charsUnescaped[i];
				return ue;
			}
		}
		return nullUnescaped;
	}

	std::string Value::EscapeString(const std::string &value)
	{
		std::string escaped;

		for (std::string::const_iterator it = value.begin(); it != value.end(); ++it)
		{
			const char &c = (*it);

			const char *&a = getEscaped(c);
			if (a[0] != '\0')
			{
				escaped += a[0];
				escaped += a[1];
			}
			else
			{
				escaped += c;
			}
		}

		return escaped;
	}
	std::string Value::UnescapeString(const std::string &value)
	{
		std::string unescaped;

		for (std::string::const_iterator it = value.begin(); it != value.end(); ++it)
		{
			const char &c = (*it);
			char c2 = '\0';
			if (it+1 != value.end())
				c2 = *(it+1);

			const char &a = getUnescaped(c, c2);
			if (a != '\0')
			{
				unescaped += a;
				if (it+1 != value.end())
					++it;
			}
			else
			{
				unescaped += c;
			}
		}

		return unescaped;
	}


	Object::Object()
	{
	}
	Object::Object(const Object &other)
	{
		for (ChildList::const_iterator it = other.children.begin(); it != other.children.end(); ++it)
		{
			const std::string &name = (*it).first;
			Node &value = *(*it).second;

			children.push_back(std::make_pair(name, value.GetCopy()));
		}
	}
	Object::Object(const Node &other)
	{
		const Object &object = other.AsObject();

		for (ChildList::const_iterator it = object.children.begin(); it != object.children.end(); ++it)
		{
			const std::string &name = (*it).first;
			Node &value = *(*it).second;

			children.push_back(std::make_pair(name, value.GetCopy()));
		}
	}
	Object::~Object()
	{
		Clear();
	}

	Node::Type Object::GetType() const
	{
		return T_OBJECT;
	}

	void Object::Add(const std::string &name, Node &node)
	{
		children.push_back(std::make_pair(name, node.GetCopy()));
	}
	void Object::Add(const std::string &name, Value node)
	{
		children.push_back(std::make_pair(name, new Value(node)));
	}
	void Object::Remove(const std::string &name)
	{
		for (ChildList::iterator it = children.begin(); it != children.end(); ++it)
		{
			if ((*it).first == name)
			{
				delete (*it).second;
				children.erase(it);
				break;
			}
		}
	}
	void Object::Clear()
	{
		for (ChildList::iterator it = children.begin(); it != children.end(); ++it)
		{
			delete (*it).second;
			(*it).second = NULL;
		}
		children.clear();
	}

	Object::iterator Object::begin()
	{
		if (!children.empty())
			return Object::iterator(&children.front());
		else
			return Object::iterator(NULL);
	}
	Object::const_iterator Object::begin() const
	{
		if (!children.empty())
			return Object::const_iterator(&children.front());
		else
			return Object::const_iterator(NULL);
	}
	Object::iterator Object::end()
	{
		if (!children.empty())
			return Object::iterator(&children.back()+1);
		else
			return Object::iterator(NULL);
	}
	Object::const_iterator Object::end() const
	{
		if (!children.empty())
			return Object::const_iterator(&children.back()+1);
		else
			return Object::const_iterator(NULL);
	}

	bool Object::Has(const std::string &name) const
	{
		for (ChildList::const_iterator it = children.begin(); it != children.end(); ++it)
		{
			if ((*it).first == name)
			{
				return true;
			}
		}
		return false;
	}
	size_t Object::GetCount() const
	{
		return children.size();
	}
	Node &Object::Get(const std::string &name) const
	{
		for (ChildList::const_iterator it = children.begin(); it != children.end(); ++it)
		{
			if ((*it).first == name)
			{
				return *(*it).second;
			}
		}
		
		throw NotFoundException();
	}

	Node *Object::GetCopy() const
	{
		return new Object(*this);
	}


	Array::Array()
	{
	}
	Array::Array(const Array &other)
	{
		for (ChildList::const_iterator it = other.children.begin(); it != other.children.end(); ++it)
		{
			const Node &value = *(*it);

			children.push_back(value.GetCopy());
		}
	}
	Array::Array(const Node &other)
	{
		const Array &array = other.AsArray();

		for (ChildList::const_iterator it = array.children.begin(); it != array.children.end(); ++it)
		{
			const Node &value = *(*it);

			children.push_back(value.GetCopy());
		}
	}
	Array::~Array()
	{
		Clear();
	}

	Node::Type Array::GetType() const
	{
		return T_ARRAY;
	}

	void Array::Add(Node &node)
	{
		children.push_back(node.GetCopy());
	}
	void Array::Add(Value node)
	{
		children.push_back(new Value(node));
	}
	void Array::Remove(size_t index)
	{
		if (index < children.size())
		{
			ChildList::iterator it = children.begin()+index;
			delete (*it);
			children.erase(it);
		}
	}
	void Array::Clear()
	{
		for (ChildList::iterator it = children.begin(); it != children.end(); ++it)
		{
			delete (*it);
			(*it) = NULL;
		}
		children.clear();
	}

	Array::iterator Array::begin()
	{
		if (!children.empty())
			return Array::iterator(&children.front());
		else
			return Array::iterator(NULL);
	}
	Array::const_iterator Array::begin() const
	{
		if (!children.empty())
			return Array::const_iterator(&children.front());
		else
			return Array::const_iterator(NULL);
	}
	Array::iterator Array::end()
	{
		if (!children.empty())
			return Array::iterator(&children.back()+1);
		else
			return Array::iterator(NULL);
	}
	Array::const_iterator Array::end() const
	{
		if (!children.empty())
			return Array::const_iterator(&children.back()+1);
		else
			return Array::const_iterator(NULL);
	}

	size_t Array::GetCount() const
	{
		return children.size();
	}
	Node &Array::Get(size_t index) const
	{
		if (index < children.size())
		{
			return *children.at(index);
		}

		throw NotFoundException();
	}

	Node *Array::GetCopy() const
	{
		return new Array(*this);
	}


	FileWriter::FileWriter()
	{
	}
	FileWriter::~FileWriter()
	{
	}

	void FileWriter::WriteFile(const std::string &filename, const Node &root, const Format &format)
	{
		FileWriter writer;
		writer.Write(filename, root, format);
	}

	void FileWriter::Write(const std::string &filename, const Node &root, const Format &format)
	{
		Writer writer(root, filename, format);
		writer.Write();

		std::fstream file(filename.c_str(), std::ios::out | std::ios::trunc);
		file << writer.GetResult();
		file.close();
	}


	FileReader::FileReader(const std::string &filename)
	{
		std::fstream file(filename.c_str(), std::ios::in | std::ios::binary);

		file.seekg(0, std::ios::end);
		std::ios::pos_type size = file.tellg();
		file.seekg(0, std::ios::beg);
		
		json.resize(static_cast<std::string::size_type>(size), '\0');
		file.read(&json[0], size);
	}
	FileReader::~FileReader()
	{
	}

	bool FileReader::ReadFile(const std::string &filename, Node &node)
	{
		FileReader reader(filename);
		return reader.Read(node);
	}

	bool FileReader::Read(Node &node)
	{
		Parser parser(node, json);
		if (!parser.Parse())
		{
			error = parser.GetError();
			return false;
		}
		else
		{
			return true;
		}
	}

	Node::Type FileReader::DetermineType()
	{
		return Node::DetermineType(json);
	}

	const std::string &FileReader::GetError() const
	{
		return error;
	}


	Writer::Writer(const Node &root, const Format &format) : root(root), fi(new FormatInterpreter)
	{
		SetFormat(format);
	}
	Writer::Writer(const Node &root, const std::string &filename, const Format &format) : root(root), fi(new FormatInterpreter)
	{
		SetFilename(filename);
		SetFormat(format);
	}
	Writer::~Writer()
	{
		delete fi;
		fi = NULL;
	}

	void Writer::SetFilename(const std::string &filename)
	{
		this->filename = filename;
	}
	void Writer::SetFormat(const Format &format)
	{
		fi->SetFormat(format);
	}
	void Writer::Write()
	{
		result.clear();
		writeNode(root, 0);
	}

	const std::string &Writer::GetResult() const
	{
		return result;
	}

	void Writer::writeNode(const Node &node, unsigned int level)
	{
		switch (node.GetType())
		{
		case Node::T_OBJECT : writeObject(node.AsObject(), level); break;
		case Node::T_ARRAY  : writeArray(node.AsArray(), level);   break;
		case Node::T_VALUE  : writeValue(node.AsValue());          break;
		}
	}
	void Writer::writeObject(const Object &node, unsigned int level)
	{
		result += "{" + fi->GetNewline();

		for (Object::const_iterator it = node.begin(); it != node.end(); ++it)
		{
			const std::string &name = (*it).first;
			const Node &value = (*it).second;

			if (it != node.begin())
				result += "," + fi->GetNewline();
			result += fi->GetIndentation(level+1) + "\""+name+"\"" + ":" + fi->GetSpacing();
			writeNode(value, level+1);
		}

		result += fi->GetNewline() + fi->GetIndentation(level) + "}";
	}
	void Writer::writeArray(const Array &node, unsigned int level)
	{
		result += "[" + fi->GetNewline();

		for (Array::const_iterator it = node.begin(); it != node.end(); ++it)
		{
			const Node &value = (*it);

			if (it != node.begin())
				result += "," + fi->GetNewline();
			result += fi->GetIndentation(level+1);
			writeNode(value, level+1);
		}

		result += fi->GetNewline() + fi->GetIndentation(level) + "]";
	}
	void Writer::writeValue(const Value &node)
	{
		if (node.IsString())
		{
			result += "\""+Value::EscapeString(node.ToString())+"\"";
		}
		else
		{
			result += node.ToString();
		}
	}


	Parser::Parser(Node &root) : root(root)
	{
	}
	Parser::Parser(Node &root, const std::string &json) : root(root)
	{
		SetJson(json);
	}
	Parser::~Parser()
	{
	}

	void Parser::SetJson(const std::string &json)
	{
		RemoveWhitespace(json, this->json);
	}
	bool Parser::Parse()
	{
		cursor = 0;

		tokenize();
		bool success = assemble();

		return success;
	}

	const std::string &Parser::GetError() const
	{
		return error;
	}

	void Parser::tokenize()
	{
		Token token;
		std::string valueBuffer;
		bool saveBuffer;

		for (; cursor < json.size(); ++cursor)
		{
			char c = json.at(cursor);

			saveBuffer = true;

			switch (c)
			{
			case '{' :
				{
					token = T_OBJ_BEGIN;
					break;
				}
			case '}' :
				{
					token = T_OBJ_END;
					break;
				}
			case '[' :
				{
					token = T_ARRAY_BEGIN;
					break;
				}
			case ']' :
				{
					token = T_ARRAY_END;
					break;
				}
			case ',' :
				{
					token = T_SEPARATOR_NODE;
					break;
				}
			case ':' :
				{
					token = T_SEPARATOR_NAME;
					break;
				}
			case '"' :
				{
					token = T_VALUE;
					readString();
					break;
				}
			default :
				{
					valueBuffer += c;
					saveBuffer = false;
					break;
				}
			}

			if ((saveBuffer || cursor == json.size()-1) && (!valueBuffer.empty())) // Always save buffer on the last character
			{
				if (interpretValue(valueBuffer))
				{
					tokens.push(T_VALUE);
				}
				else
				{
					// Store the unknown token, so we can show it to the user
					data.push(std::make_pair(Value::VT_STRING, valueBuffer));
					tokens.push(T_UNKNOWN);
				}

				valueBuffer.clear();
			}

			// Push the token last so that any
			// value token will get pushed first
			// from above.
			// If saveBuffer is false, it means that
			// we are in the middle of a value, so we
			// don't want to push any tokens now.
			if (saveBuffer)
			{
				tokens.push(token);
			}
		}
	}
	bool Parser::assemble()
	{
		std::stack<std::pair<std::string, Node*> > nodeStack;

		std::string name = "";

		Token token;
		while (!tokens.empty())
		{
			token = tokens.front();
			tokens.pop();

			switch (token)
			{
			case T_UNKNOWN :
				{
					const std::string &unknownToken = data.front().second;
					error = "Unknown token: "+unknownToken;
					data.pop();
					return false;
				}
			case T_OBJ_BEGIN :
				{
					Node *node = NULL;
					if (nodeStack.empty())
					{
						if (!root.IsObject())
						{
							error = "The given root node is not an object";
							return false;
						}

						node = &root;
					}
					else
					{
						node = new Object;
					}

					nodeStack.push(std::make_pair(name, node));
					name.clear();
					break;
				}
			case T_ARRAY_BEGIN :
				{
					Node *node = NULL;
					if (nodeStack.empty())
					{
						if (!root.IsArray())
						{
							error = "The given root node is not an array";
							return false;
						}

						node = &root;
					}
					else
					{
						node = new Array;
					}

					nodeStack.push(std::make_pair(name, node));
					name.clear();
					break;
				}
			case T_OBJ_END :
			case T_ARRAY_END :
				{
					if (nodeStack.empty())
					{
						error = "Found end of object or array without beginning";
						return false;
					}
					if (token == T_OBJ_END && !nodeStack.top().second->IsObject())
					{
						error = "Mismatched end and beginning of object";
						return false;
					}
					if (token == T_ARRAY_END && !nodeStack.top().second->IsArray())
					{
						error = "Mismatched end and beginning of array";
						return false;
					}

					std::string name = nodeStack.top().first;
					Node *node = nodeStack.top().second;
					nodeStack.pop();

					if (!nodeStack.empty())
					{
						if (nodeStack.top().second->IsObject())
						{
							nodeStack.top().second->AsObject().Add(name, *node);
						}
						else if (nodeStack.top().second->IsArray())
						{
							nodeStack.top().second->AsArray().Add(*node);
						}
						else
						{
							error = "Can only add elements to objects and arrays";
							return false;
						}

						delete node;
						node = NULL;
					}
					break;
				}
			case T_VALUE :
				{
					if (!tokens.empty() && tokens.front() == T_SEPARATOR_NAME)
					{
						tokens.pop();
						if (data.front().first != Value::VT_STRING)
						{
							error = "A name has to be a string";
							return false;
						}
						else
						{
							name = data.front().second;
							data.pop();
						}
					}
					else
					{
						Node *node = NULL;
						if (nodeStack.empty())
						{
							if (!root.IsValue())
							{
								error = "The given root node is not a value";
								return false;
							}

							node = &root;
						}
						else
						{
							node = new Value;
						}

						if (data.front().first == Value::VT_STRING)
						{
							static_cast<Value*>(node)->Set(data.front().second); // This method calls UnescapeString()
						}
						else
						{
							static_cast<Value*>(node)->Set(data.front().first, data.front().second);
						}
						data.pop();

						if (!nodeStack.empty())
						{
							if (nodeStack.top().second->IsObject())
								nodeStack.top().second->AsObject().Add(name, *node);
							else if (nodeStack.top().second->IsArray())
								nodeStack.top().second->AsArray().Add(*node);

							delete node;
							node = NULL;
							name.clear();
						}
						else
						{
							nodeStack.push(std::make_pair(name, node));
							name.clear();
						}
					}
					break;
				}
			}
		}

		return true;
	}

	void Parser::readString()
	{
		if (json.at(cursor) != '"')
			return;

		std::string str;

		++cursor;

		char c1 = '\0';
		for (; cursor < json.size(); ++cursor)
		{
			char c2 = json.at(cursor);

			if (c1 != '\\' && c2 == '"')
			{
				break;
			}

			str += c2;

			c1 = c2;
		}

		data.push(std::make_pair(Value::VT_STRING, str));
	}

	bool Parser::interpretValue(const std::string &value)
	{
		std::string upperValue(value.size(), '\0');

		std::transform(value.begin(), value.end(), upperValue.begin(), toupper);

		if (upperValue == "NULL")
		{
			data.push(std::make_pair(Value::VT_NULL, ""));
		}
		else if (upperValue == "TRUE")
		{
			data.push(std::make_pair(Value::VT_BOOL, "true"));
		}
		else if (upperValue == "FALSE")
		{
			data.push(std::make_pair(Value::VT_BOOL, "false"));
		}
		else
		{
			bool number = true;
			for (std::string::const_iterator it = value.begin(); it != value.end(); ++it)
			{
				if (!isNumber(*it))
				{
					number = false;
					break;
				}
			}

			if (number)
			{
				data.push(std::make_pair(Value::VT_NUMBER, value));
			}
			else
			{
				return false;
			}
		}

		return true;
	}

	bool Parser::isNumber(char c) const
	{
		return ((c >= '0' && c <= '9') || c == '.' || c == '-');
	}
}