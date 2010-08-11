subplot(1,2,1)
imagesc(state(:,:,2))
state(1,1,2) = max( max(state(:,:,1)));
title('Away Goals')
ylabel('Game')
xlabel('Time (min)')
subplot(1,2,2)
imagesc(state(:,:,1))
% lazily making sure that the colormaps line up right
title('Home Goals')
xlabel('Time (min)')