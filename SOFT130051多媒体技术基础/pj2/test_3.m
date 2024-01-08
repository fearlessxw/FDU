% Enhance the contrast of a given image using grayscale stretching and 
% histogram equalization respectively.
% Provide comparison pictures before and after enhancement, as well as gray 
% value distribution diagram.

% input grayscale image
imname = "my_gray_satomi.jpg";

% Read the image
I = imread(imname);

% Adjust the image using imadjust: from [0.2,0.6] to [0,1]
% Values below 0.2 become 0, and values above 0.6 become 1
% The pixel values are scaled to the range [0, 1] in the output image
J = imadjust(I, [0.2, 0.6], [0, 1]);

% Histogram equalization
H = histeq(I);

% Display the image and distribution
% use imshow() to display the original image
% use imhist() to get the gray value distribution diagram
figure
subplot(2, 3, 1), imshow(I), title("Original Image");
subplot(2, 3, 2), imshow(J), title("Contrast Stretching");
subplot(2, 3, 3), imshow(H), title("Histogram Equalization");
subplot(2, 3, 4), imhist(I);
subplot(2, 3, 5), imhist(J);
subplot(2, 3, 6), imhist(H);

% Save figure
saveas(gcf, 'test_3_result.jpg');