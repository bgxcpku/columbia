function S = visswendsenwang(J,observed,labels,burn,iters,X)
% function S = visswendsenwang(J,observed,labels,burn,iters,X)
% Generalised Swendsen-Wang sampler for Ising models (spins of +/-1) with
% positive and negative connections. With visualisation of clusters
%
% REMEMBER to use {+1,-1} spins (NOT {0,1}) or this will break horribly.
% 
% INPUTS
%        J nxn --- coupling matrix
% observed nx1 --- mask giving variables that are observed (!=0 => observed)
%   labels nx1 --- labels for observed variables (non-observeds are used as
%                  start state)
%     burn 1x1 --- number of iterations to discard
%    iters 1x1 --- number of iterations for which to run after burn steps
%        X nx2 --- positions of variables for visualisation
%
% OUTPUTS
%        S nxiters --- samples over labels
%
% Iain Murray --- March 2004

clf; hold on; axis off;
ball=25;
thick=2;
set(gcf,'Color',[1,1,1]);
n=size(J,1);
S=zeros(n,iters);
D=zeros(n,n);

% Split up weights into positive and negative parts
Jpos=J.*(J>0); Jneg=J.*(J<0);
pmask=find(tril(Jpos,-1)); nmask=find(tril(Jneg,-1));

% Use labels provided by user for initial condition
Y=labels;
scatter(X(find(Y==+1),1),X(find(Y==+1),2),ball,'ro','filled')
scatter(X(find(Y==-1),1),X(find(Y==-1),2),ball,'bo','filled')

for s=1:(burn+iters)
	% Sample from the links between clusters
	Ymatch=(repmat(Y,1,n)==repmat(Y',n,1));
	Yunmatch=(repmat(Y,1,n)~=repmat(Y',n,1));
	U=rand(n,n);
	D=zeros(n,n); E=zeros(n,n);
	D(pmask)=(U(pmask)<(1-exp(-2*Jpos(pmask)))).*Ymatch(pmask);
	E(nmask)=(U(nmask)<(1-exp(2*Jneg(nmask)))).*Yunmatch(nmask);
	D=D+D'+eye(n); E=E+E'; F=D+E;
	
	% Sample from the variables given the links
	sampled=zeros(n,1);
	for i=1:n
		if ~sampled(i)
			% find all of connected component owning this point
			cluster=i;
			newcluster=find(F(i,:));
			while (~isequal(cluster,newcluster))
				cluster=newcluster;
				newcluster=find(sum(F(:,cluster),2));
			end
			% Mark every point in this cluster done
			sampled(cluster)=1;
			% If we have label in cluster that dictates everything
			% about it so we leave it. Otherwise we flip all lables
			% with probability 1/2.
			existing=find(observed.*sum(F(:,cluster),2));
			if min(size(existing))==0
				if (rand<0.5)
					Y(cluster)=-Y(cluster);
				end
			end
		end
	end
	
        axis 'square'
	printmenow();
	[from,to]=ind2sub([n,n],find(F));
	for i=1:size(from,1)
		plot([X(from(i),1),X(to(i),1)],[X(from(i),2),X(to(i),2)],'k-','LineWidth',thick)
	end
	scatter(X(:,1),X(:,2),thick*thick,'ko','filled')
        axis 'square'
	printmenow();
	clf; hold on; axis off;
	for i=1:size(from,1)
		plot([X(from(i),1),X(to(i),1)],[X(from(i),2),X(to(i),2)],'k-','LineWidth',thick)
	end
	scatter(X(:,1),X(:,2),thick*thick,'ko','filled')
        axis 'square'
	printmenow();
	scatter(X(find(Y==+1),1),X(find(Y==+1),2),ball,'ro','filled')
	scatter(X(find(Y==-1),1),X(find(Y==-1),2),ball,'bo','filled')
        axis 'square'
	printmenow();
	clf; hold on; axis off;
	scatter(X(find(Y==+1),1),X(find(Y==+1),2),ball,'ro','filled')
	scatter(X(find(Y==-1),1),X(find(Y==-1),2),ball,'bo','filled')
	
	% Store sample
	if s>burn
		S(:,s-burn)=Y;
	end
end
