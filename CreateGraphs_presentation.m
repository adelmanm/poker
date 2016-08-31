clear all;
close all;
INFOSETS_PATH = '..\logs\infosets.csv';
ALGORITHMS_PATH = '..\logs\algorithms.csv';
LOGS_PATH = '..\logs\';
%% plot informtion sets strategies - Kuhn
ITERATION_GAP = 1;
[~,~,raw]=xlsread(INFOSETS_PATH);
infosets=raw(:,1);
N = length(infosets);

infoset_legend={0};
figure;
hold on;
title('Player 1 bet strategies');
xlabel('iterations');
ylabel('strategy');
for i = 1:N
    if isnumeric(infosets{i})
        infosets{i}=num2str(infosets{i});
    end
    strategy_path = strcat(LOGS_PATH,infosets{i},'_strategy.csv');
    strategy_data = importdata(strategy_path);
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
[~,~,algorithms]=xlsread(ALGORITHMS_PATH);
% algorithms=raw(:,1);
N = length(algorithms);

algorithms_legend={0};
figure;
hold on;
title('First player utilities');
xlabel('Visited nodes');
ylabel('utility');
for i=1:N
    util_path = strcat(LOGS_PATH, algorithms{i}, '_util_hist.csv');
    [~,~,raw]=xlsread(util_path);
    util_data = cell2mat(raw(:,1));
    visited_nodes=cell2mat(raw(:,4));

    plot(visited_nodes,util_data,'LineWidth',1,'Marker','.','MarkerSize',20);
    algorithms_legend{end+1}=algorithms{i};
    text(visited_nodes(end)*(1.01), util_data(end,1), num2str(util_data(end,1)),'FontSize',14);
    drawnow;
end
legend(algorithms_legend{2:end});