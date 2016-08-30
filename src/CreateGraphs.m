clear all;
close all;
INFOSETS_PATH = 'logs\infosets.csv';
LOGS_PATH = 'logs\';
%% plot informtion sets strategies
ITERATION_GAP = 1;
infosets = textread(INFOSETS_PATH, '%s', 'delimiter', '\n');
N = length(infosets);
for i = 1:N
    strategy_path = strcat(LOGS_PATH,infosets(i),'_strategy.csv');
    strategy_data = importdata(strategy_path{1});
    figure(i);
    hold on;
    title(infosets(i));
    xlabel('iterations');
    ylabel('strategy');
    iterations = (1:size(strategy_data,1)).*ITERATION_GAP;
    plot(iterations,strategy_data,'LineWidth',1,'Marker','.','MarkerSize',20);
    legend('check','bet','call','fold','raise');
    drawnow;
    
end
%% plot player utilities
util_path = strcat(LOGS_PATH,'util_hist.csv');
util_data = importdata(util_path);
figure(i+1);
hold on;
title('Player utilities');
xlabel('iterations');
ylabel('utility');
iterations = 1:size(util_data,1);
plot(iterations,util_data,'LineWidth',1,'Marker','.','MarkerSize',20);
legend('player0','player1');
drawnow;