function x=inv_ulaw(y,u)
%INV_ULAW		the inverse of u-law nonlinearity
%X=INV_ULAW(Y,U)	X=normalized output of the u-law nonlinearity.

% todo: 
    % Apply the inverse of u-law compression to decode the signal
    x = sign(y) .* (1/u) .* ((1 + u).^abs(y) - 1);

end