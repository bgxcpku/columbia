function X = plus_diag(X,y)
% function X = plus_diag(X,y)
%
% This function does "X = X + diag(y)" but more efficiently when X is very
% large.
%
% Inputs:
% 	 X NxN 
% 	 y Nx1, 1xN or 1x1
%
% Outputs:
% 	 X  NxN = X + diag(y)

% Iain Murray, June 2006

[N,M]=size(X);
if N~=M, error('X must be square'), end

diagidx=(0:N-1)*N+(1:N);
X(diagidx)=X(diagidx)+y(:)';
