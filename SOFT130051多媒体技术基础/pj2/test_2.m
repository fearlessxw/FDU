% transform the rgb image into greyscale image
% calculate the variance of the converted grayscale image

% input path of image: satomi.jpg
rgbImage = imread("satomi.jpg");

% check whether the image is RGB img
if size(rgbImage, 3) ~= 3
    error('Please input a RGB image');
end

% transfer into gray image using luminance formula
grayImage = 0.299 * rgbImage(:,:,1) + 0.587 * rgbImage(:,:,2) + 0.114 * rgbImage(:,:,3);
grayImage = uint8(grayImage);

% calculate the variance of grayscale image using std2()function
varianceValue = std2(grayImage)^2;
fprintf('variance of grayscale image: %f\n', varianceValue);

% save the grayscale image
imwrite(grayImage, 'my_gray_satomi.jpg');
% display both the rgb and grayscale image
figure;
subplot(1, 2, 1), imshow(rgbImage), title('原图像');
subplot(1, 2, 2), imshow(grayImage), title('灰度图像');