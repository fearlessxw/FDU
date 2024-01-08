% Add salt and pepper noise to a given image, and choose at least two 
% denoising algorithms you know to remove salt and pepper noise.
% Provide a comparison chart of the results after denoising, and analyze 
% the reasons for this difference through the algorithm

% read original image
originalImage = imread('satomi.jpg');

% --- ADD NOISE --- 
% add noise to image (salt&pepper noise, 0.04 density) using imnoise()
noisyImage = imnoise(originalImage, 'salt & pepper', 0.04);
% display both original image and noisy image
figure;
subplot(1, 2, 1), imshow(originalImage), title('原始图像');
subplot(1, 2, 2), imshow(noisyImage), title('带椒盐噪声的图像');

% --- DENOISE ---

% 1: mean filter with 3*3 window
averageFiltered = imfilter(noisyImage, fspecial('average', [3, 3]));
figure;
subplot(1, 3, 1), imshow(originalImage), title('原始图像');
subplot(1, 3, 2), imshow(noisyImage), title('加椒盐噪声的图像');
subplot(1, 3, 3), imshow(averageFiltered), title('均值滤波去噪后的图像');


% 2: median filter with 3*3 window
% Since medfilt2 only supports 2-dimensional matrices, filter R/G/B channels separately
medianFiltered(:, :, 1) = medfilt2(noisyImage(:, :, 1), [3 3]);
medianFiltered(:, :, 2) = medfilt2(noisyImage(:, :, 2), [3 3]);
medianFiltered(:, :, 3) = medfilt2(noisyImage(:, :, 3), [3 3]);
figure;
subplot(1, 3, 1), imshow(originalImage), title('原始图像');
subplot(1, 3, 2), imshow(noisyImage), title('带椒盐噪声的图像');
subplot(1, 3, 3), imshow(medianFiltered), title('中值滤波去噪后的图像');

% 3: Gaussian filter with a standard deviation (σ) of 1
denoisedGaussian = imgaussfilt(noisyImage, 1);
figure;
subplot(1, 3, 1), imshow(originalImage), title('原始图像');
subplot(1, 3, 2), imshow(noisyImage), title('带椒盐噪声的图像');
subplot(1, 3, 3), imshow(denoisedGaussian), title('高斯滤波去噪后的图像');

