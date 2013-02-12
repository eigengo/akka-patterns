				Mat grad_x, grad_y;
				Mat sourceGray;
				Mat grad;
				Mat abs_grad_x, abs_grad_y;
				int scale = 1;
				int delta = 0;
				int ddepth = CV_16S;
				// source -> grayscale
				Mat source = imread(fileName);
				cvtColor(source, sourceGray, CV_RGB2GRAY);
				
				// Gradient X
				Sobel(sourceGray, grad_x, ddepth, 1, 0, 3, scale, delta, BORDER_DEFAULT);
				convertScaleAbs(grad_x, abs_grad_x);
				
				// Gradient Y
				Sobel(sourceGray, grad_y, ddepth, 0, 1, 3, scale, delta, BORDER_DEFAULT);
				convertScaleAbs(grad_y, abs_grad_y);
				
				// Total Gradient (approximate)
				addWeighted(abs_grad_x, 0.5, abs_grad_y, 0.5, 0, grad);
