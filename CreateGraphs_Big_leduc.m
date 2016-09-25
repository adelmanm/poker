clear all;
close all;
ALGORITHMS_PATH = '..\algorithms.xlsx';
LOGS_PATH = '..\logs';
%%
algorithm_num=1;
[~,~,algorithms]=xlsread(ALGORITHMS_PATH);
util_path = strcat(LOGS_PATH,'_', algorithms{algorithm_num}, '\util_hist.csv');
[~,~,raw]=xlsread(util_path);
visited_nodes=cell2mat(raw(:,4));
INFOSETS_PATH=strcat(LOGS_PATH,'_', algorithms{algorithm_num}, '\infosets.csv');
[~,~,raw]=xlsread(INFOSETS_PATH);
infosets=raw(:,1);
infosets_to_show_ind=[1878];
for i = 1:length(infosets_to_show_ind);
    if isnumeric(infosets{infosets_to_show_ind(i)})
        infosets{infosets_to_show_ind(i)}=num2str(infosets{infosets_to_show_ind(i)});
    end
    figure;
    strategy_path = strcat(LOGS_PATH,'_', algorithms{algorithm_num},'\', infosets(infosets_to_show_ind(i)),'_strategy.csv');
    [~,~,raw]=xlsread(cell2mat(strategy_path)); 
    strategy_data = cell2mat(raw);
    plot(visited_nodes(1:end-1),strategy_data,'LineWidth',1,'Marker','.');
%     plot(visited_nodes(1:30),strategy_data(1:30,:),'LineWidth',1,'Marker','.');
    drawnow;
    title(strcat(algorithms{algorithm_num},' infoset  ', infosets{infosets_to_show_ind(i)} ), 'FontSize', 14);
    xlabel('Visited nodes');
    ylabel('strategy');
    legend('check', 'bet', 'Fold', 'Call', 'Raise');
end

%% plot player utilities 

[~,~,algorithms]=xlsread(ALGORITHMS_PATH);
% algorithms=raw(:,1);
N = length(algorithms);

algorithms_legend={0};
figure;
hold on;
cc= lines(10);
title('First player utilities');
xlabel('Visited nodes');
ylabel('utility');
for i=1:N
    util_path = strcat(LOGS_PATH,'_', algorithms{i}, '\util_hist.csv');
%     algorithms{i}
    [~,~,raw]=xlsread(util_path);
    util_data = cell2mat(raw(:,1));
    visited_nodes=cell2mat(raw(:,4));
    util_length=length(util_data);
    num_of_segments=10;
    segment_length=floor(util_length/num_of_segments);
    eps_convergence=10^-3;
    plot_end_ind=util_length;
    for j=1:num_of_segments-1
       initial_j=(j-1)*segment_length+1;
       util_data_segment=util_data(initial_j:initial_j+segment_length);
       if std(util_data_segment)<eps_convergence
           plot_end_ind=initial_j+segment_length;
           break;
       end 
    end    
%     plot(visited_nodes,util_data,'LineWidth',1,'Marker','.','MarkerSize',1);
    plot(visited_nodes(1:plot_end_ind),util_data(1:plot_end_ind), 'color',cc(i,:) );
    algorithms_legend{end+1}=algorithms{i};
    text(visited_nodes(plot_end_ind)*(1.01), util_data(end,1), num2str(util_data(end,1)),'FontSize',14);
    drawnow;
end
legend(algorithms_legend{2:end});