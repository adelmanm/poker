clear all;
close all;
INFOSETS_PATH = '..\logs\infosets.csv';
LOGS_PATH = '..\logs\';
%% plot informtion sets strategies - Kuhn
ITERATION_GAP = 1;
infosets = textread(INFOSETS_PATH, '%s', 'delimiter', '\n');
N = length(infosets);

infoset_legend={0};
figure;
hold on;
title('Player 1 bet strategies');
xlabel('iterations');
ylabel('strategy');
for i = 1:N
    strategy_path = strcat(LOGS_PATH,infosets(i),'_strategy.csv');
    strategy_data = importdata(strategy_path{1});
    iterations = (1:size(strategy_data,1)).*ITERATION_GAP;
    if length(infosets{i})==2
        infoset_legend{end+1}=infosets{i};
        plot(iterations,strategy_data(:,2),'LineWidth',1,'Marker','.','MarkerSize',20);
        drawnow;
    end
end
legend(infoset_legend{2:end});

infoset_legend={0};
figure;
hold on;
title('Player 0 bet strategies');
xlabel('iterations');
ylabel('strategy');
for i = 1:N
    strategy_path = strcat(LOGS_PATH,infosets(i),'_strategy.csv');
    strategy_data = importdata(strategy_path{1});
    iterations = (1:size(strategy_data,1)).*ITERATION_GAP;
    if length(infosets{i})~=2
        infoset_legend{end+1}=infosets{i};
        plot(iterations,strategy_data(:,2),'LineWidth',1,'Marker','.','MarkerSize',20);
        drawnow;
    end
end
legend(infoset_legend{2:end});
%% plot player utilities 
ITERATION_GAP = 1; % 1 for Kuhn, 100 for Leduc
util_path = strcat(LOGS_PATH,'util_hist.csv');
util_data = importdata(util_path);
figure(i+1);
hold on;
title('Player utilities');
xlabel('iterations');
ylabel('utility');
iterations = (1:size(util_data,1))*ITERATION_GAP;
plot(iterations,util_data,'LineWidth',1,'Marker','.','MarkerSize',20);
legend('player0','player1');
text(length(iterations)*ITERATION_GAP+20, util_data(end,1), num2str(util_data(end,1)),'FontSize',14);
text(length(iterations)*ITERATION_GAP+20, util_data(end,2), num2str(util_data(end,2)),'FontSize',14);
drawnow;