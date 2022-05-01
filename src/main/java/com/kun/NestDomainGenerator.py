import os

N = 10
if __name__ == '__main__':
	domain = 'Nest{}'.format(str(N))
	declare_list = [str(N)]
	for i in range(1, N + 1):
		declare_list.append('x{} 1'.format(str(i)))
	declare = ' '.join(declare_list)
	init = declare
	goal_list = [str(N)]
	for i in range(1, N + 1):
		goal_list.append('x{} 0'.format(str(i)))
	goal = ' '.join(goal_list)
	act_list = [str(N)]
	for i in range(1, N + 1):
		name = 'act' + str(i)
		pre_list = ['{} x{} 1'.format(str(N + 1 - i), str(i))]
		for j in range(i + 1, N + 1):
			pre_list.append('x{} 0'.format(str(j)))
		pre = ' '.join(pre_list)
		eff_list = ['{} x{} 0'.format(str(N + 1 - i), str(i))]
		for j in range(i + 1, N + 1):
			eff_list.append('x{} 1'.format(str(j)))
		eff = ' '.join(eff_list)
		act_list.append('{}\n{}\n{}'.format(name, pre, eff))
	act = '\n'.join(act_list)
	content = '{}\n{}\n{}\n{}\n{}'.format(domain, declare, init, goal, act)

	with open('{}.qnp'.format(domain), 'w') as f:
		f.write(content)
