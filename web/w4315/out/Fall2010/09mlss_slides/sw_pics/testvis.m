rand('state',0); % helps debugging to get same every time
%m=19;
m=50;
n=m*m;
strength=0.4;
X=zeros(2,n); % note these dimensions are swapped. size(X)==zeros(n,2)
[X(1,:),X(2,:)]=ind2sub([m,m],1:n); X=X';
D=squeeze(sqrt(sum((repmat(X,[1,1,n])-repmat(shiftdim(X',-1),[n,1,1])).^2,2)));
%J=(D==1)*strength+(D==(m-1))*strength;
J=(D==1)*strength; % don't use periodic bc's
                   % as don't know how to visualise properly
observed=zeros(n,1);
labels=((rand(n,1)<0.5)*2-1);
burn=0;
iters=30;

%S = visswendsenwang(J,observed,labels,burn,iters,X);
S = fastvis(J,observed,labels,burn,iters,X);

