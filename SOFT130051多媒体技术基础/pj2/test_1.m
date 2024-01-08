% Given a picture and a position(x,y), output the R, G, and B values ​​of 
% this point and 8 points around (x, y)
% Both the picture and position are assigned by user
% Pay attention to boundary examination


% input path of image by user
imagePath = input('image:', 's');
% read the image
img = imread(imagePath);

% get the size of image
[rows, cols, ~] = size(img);
    
% input the loc by user
x = input('x:');
y = input('y:');

% boundary examination for the specified coordinates (x, y)
% the position should be within the image
if x < 1 || x > cols || y < 1 || y > rows
    disp('错误：坐标超出图片范围。');
    return;
end

% get the RGB values of the specified point (x, y) and its surrounding 8 points
for i = -1:1
    for j = -1:1
        newX = x + i;
        newY = y + j;
            
        % boundary examination for the surrounding points
        % ignore the point if it is out of image
        if newX >= 1 && newX <= cols && newY >= 1 && newY <= rows
            fprintf('(%d, %d): (%d, %d, %d)\n', newX, newY, img(newY, newX, :));
        end
    end
end