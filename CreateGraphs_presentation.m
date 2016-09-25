clear all;
close all;
INFOSETS_PATH = '..\logs\infosets.csv';
ALGORITHMS_PATH = '..\algorithms.xlsx';
LOGS_PATH = '..\logs';
%% plot informtion sets strategies - Kuhn
algorithm_num=1;
[~,~,algorithms]=xlsread(ALGORITHMS_PATH);
util_path = strcat(LOGS_PATH,'_', algorithms{algorithm_num}, '\util_hist.csv');
[~,~,raw]=xlsread(util_path);
visited_nodes=cell2mat(raw(:,4));
INFOSETS_PATH=strcat(LOGS_PATH,'_', algorithms{algorithm_num}, '\infosets.csv');
[~,~,raw]=xlsread(INFOSETS_PATH);
infosets=raw(:,1);
N = length(infosets);

infoset_legend={0};
figure;
hold on;
cc= prism(N);
title('Player 1 bet strategies', 'FontSize', 14);
xlabel('Visited nodes');
ylabel('strategy');
for i = 1:N
    if isnumeric(infosets{i})
        infosets{i}=num2str(infosets{i});
    end
    strategy_path = strcat(LOGS_PATH,infosets{i},'_strategy.csv');
    strategy_path = strcat(LOGS_PATH,'_', algorithms{algorithm_num},'\', infosets(i),'_strategy.csv');
    [~,~,raw]=xlsread(cell2mat(strategy_path)); 
    strategy_data = cell2mat(raw);
    if length(infosets{i})==2
        infoset_legend{end+1}=infosets{i};
        plot(visited_nodes(1:length(strategy_data)),strategy_data(:,2),'LineWidth',1,'Marker','.', 'MarkerSize', 20, 'color', cc(i,:));
        drawnow;
    end
end
legend(infoset_legend{2:end});
hold off;

infoset_legend={0};
figure;
hold on;
title('Player 0 bet strategies', 'FontSize', 14);
xlabel('Visited nodes');
ylabel('strategy');
for i = 1:N
    if isnumeric(infosets{i})
        infosets{i}=num2str(infosets{i});
    end
    strategy_path = strcat(LOGS_PATH,infosets{i},'_strategy.csv');
    strategy_path = strcat(LOGS_PATH,'_', algorithms{algorithm_num},'\', infosets(i),'_strategy.csv');
    [~,~,raw]=xlsread(cell2mat(strategy_path)); 
    strategy_data = cell2mat(raw);
    if length(infosets{i})~=2
        infoset_legend{end+1}=infosets{i};
        plot(visited_nodes(1:length(strategy_data)),strategy_data(:,1),'LineWidth',1,'Marker','.','MarkerSize', 20, 'color', cc(i,:));
        drawnow;
    end
end
legend(infoset_legend{2:end});
hold off;
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
    util_path = strcat(LOGS_PATH,'_', algorithms{i}, '\util_hist.csv');
    [~,~,raw]=xlsread(util_path);
    util_data = cell2mat(raw(:,1));
    visited_nodes=cell2mat(raw(:,4));

    plot(visited_nodes,util_data,'LineWidth',1,'Marker','.','MarkerSize',20);
    algorithms_legend{end+1}=algorithms{i};
    text(visited_nodes(end)*(1.01), util_data(end,1), num2str(util_data(end,1)),'FontSize',14);
    drawnow;
end
legend(algorithms_legend{2:end});