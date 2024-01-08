function [z]=ulaw(y,u)
%		u-law nonlinearity for nonuniform PCM
%		X=ULAW(Y,U).
%		Y=input vector.

% todo: 
    % Apply u-law compression to the input signal
    z = sign(y) .* log(1 + u * abs(y)) / log(1 + u);
end