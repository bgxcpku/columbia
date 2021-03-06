function message = get_message_fn_am(to_node);
% Calculates a message to be sent to the argument node from the factor node
% between variable nodes a and m.
%
% @param     to_node :   variable node to which a message is being passed
% @return    message :   message to pass to to_node, MUST BE COLUMN VECTOR

global fn_am

node = fn_am;

% this for loops ensures that all the messages needed to pass the requested
% message are up to date
for i = 1 : length(node.c)
    if ~strcmp(node.c{i}.name,to_node.name)
        node.m{i} = get_message_vn(node,node.c{i});
    end
end

% now all the child messages are up to date, calculate the message to be
% sent to to_node