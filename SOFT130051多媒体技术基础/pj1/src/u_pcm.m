function [a_quan]=u_pcm(a,n)
%U_PCM  	uniform PCM encoding of a sequence
%       	[A_QUAN]=U_PCM(A,N)
%       	a=input sequence.
%       	n=number of quantization levels (even).
%		a_quan=quantized output before encoding.

% todo: 

% Calculate the step size (quantization interval)
step_size = (max(a) - min(a)) / n;

% Calculate the midpoints of quantization intervals
midpoints = min(a) + (step_size / 2):step_size:max(a) - (step_size / 2);

% Initialize the quantized output
a_quan = zeros(size(a));

% Quantize the input sequence 'a' using uniform PCM
for i = 1:length(a)
    % Find the closest midpoint to the current input sample
    [~, index] = min(abs(a(i) - midpoints));
    
    % Assign the quantized value (midpoint) to the output
    a_quan(i) = midpoints(index);
end

end