% create a series of time points for one complete cycle of a cosine wave
t=0:0.0001:2*pi; 
% computes the cosine values at each time point in the t vector, resulting in a cosine wave stored in the y variable
y=cos(t);

z1=u_pcm(y,64);
z2=ula_pcm(y,64,255);
plot(t,y,t,z1,'r',t,z2,'g');