import random
from sys import maxint
from math import log
from special import lgamma, stirling
from collections import deque

def mh_sample(seq,num_samples,a,a_0,b,init=None):
	if init == None:
		if type(seq[0]) == list:
			num_symbols = max(max(subseq) for subseq in seq) + 1
		else:
			num_symboles = max(seq) + 1
		old_machine = PDFA(num_symbols,a,a_0,b)
		pdfas = []
	else:
		old_machine = init.deepcopy()
		pdfas = [init]
	scores = [old_machine.scoreseq(seq)]
	numstates = [old_machine.numstates()]
	alpha_0s = [old_machine.alpha_0]
	alphas = [old_machine.alpha]
	betas = [old_machine.beta]
	for t in range(num_samples):
		print 'Sweep:', t, 'out of', num_samples, '\n\t', numstates[-1], 'states\n\talpha_0 =', old_machine.alpha_0, '\n\talpha = ', old_machine.alpha, '\n\tbeta = ', old_machine.beta, '\n\tTraining log likelihood = ', scores[-1], '\n'
		machine = old_machine.deepcopy()
		for pair in old_machine.t:
			if pair in machine.t:
				for i in range(5): # sample multiple possible assignments for this state/symbol transition given the rest of the transitions
					new_machine = machine.deepcopy()
					new_machine.removepair(pair) # the state following this pair will be resampled when we call new_machine.count
					machine,new_score,new_numstates = sample_machine(machine,new_machine,seq,scores[-1])
					scores.append(new_score)
					numstates.append(new_numstates)
		old_machine = machine
		for symbol in range(old_machine.S):
			for table in old_machine.k[symbol]:
				if table in machine.k:
					for i in range(5): # do this a few times too
						dish = machine.k[symbol][table]
						new_machine = machine.deepcopy()
						del new_machine.k[symbol][table]
						new_machine.m[dish] -= 1
						if new_machine.m[dish] == 0:
							del new_machine.m[dish]
						new_dish = crp(new_machine.m,new_machine.alpha_0)
						new_machine.k[symbol][table] = new_dish
						if new_dish in new_machine.m:
							new_machine.m[new_dish] += 1
						else:
							new_machine.m[new_dish] = 1
						machine,new_score,new_numstates = sample_machine(machine,new_machine,seq,scores[-1])
						scores.append(new_score)
						numstates.append(new_numstates)
		old_machine = machine
		old_machine.sample_alpha_0()
		old_machine.sample_alpha()
		old_machine.sample_beta(seq,scores[-1])
		alpha_0s.append(old_machine.alpha_0)
		alphas.append(old_machine.alpha)
		betas.append(old_machine.beta)
		if t % 10 == 9:
			pdfas.append(machine)
	return [pdfas, scores, numstates, alpha_0s, alphas, betas]

def sample_machine(old_machine,machine,seq,old_score):
        counts = machine.count(seq)
	score = machine.score(counts)
        if log(random.random()) < score - old_score: # accept the sample
                map(machine.removepair,[x for x in counts if counts[x] == 0])
                return (machine,score,machine.numstates())
        else:
                return (old_machine,old_score,old_machine.numstates())

class PDFA:
	def __init__(self, num_symbols=2, a=1.0, a_0=1.0, b=1.0): # if any of the hyperparameters are set to None, place a vague Gamma prior on them and sample
		self.S = num_symbols
		self.t = {} # maps a state/symbol tuple to a table index in that restaurant
		self.n = [{} for i in range(self.S)] # maps a table index to the count at that table
		self.k = [{} for i in range(self.S)] # maps a table index to the table index in the high-level restaurant
		self.m = {} # maps a high-level table index to a count
		if a != None:
			self.alpha = float(a)
		if a_0 != None:
			self.alpha_0 = float(a_0)
		if b != None:
			self.beta = float(b)

	def printtransition(self):
		for x,y in self.t:
			print x,y,'->',self.next(x,y)
		print ''

	def deepcopy(self):
		copy = PDFA(self.S,self.alpha,self.alpha_0,self.beta)
		copy.t = self.t.copy()
		copy.n = [x.copy() for x in self.n]
		copy.k = [x.copy() for x in self.k]
		copy.m = self.m.copy()
		return copy
        
	def numstates(self):
		return len([i for i in self.m if self.m[i] != 0])

	def add(self,state,symbol,next_state=None,next_table=None): # If next_state is not given, sample new state according to CRF.  Else, assign a table by conditioning on next_state.
		if next_state == None:
			if next_table == None:
				table = crp(self.n[symbol],self.alpha)
			else:
				table = next_table
		else:
			dish = next_state
			if next_table != None:
				table = next_table
			elif next_state in self.k[symbol].values():
				prob = dict([(i,self.n[symbol][i]) for i in self.n[symbol] if self.k[symbol][i] == next_state]) # counts the number of times a customer sits at a table serving the dish next_state
				prob[gensym(prob)] = self.alpha * self.m[next_state] / ( sum(self.m.values()) + self.alpha_0 ) # probability of seating a customer at a new table *and* that table serving dish next_state
				table = discrete_sample(prob)
			else:
				table = gensym(self.n[symbol])
		self.t[(state,symbol)] = table
		if table in self.n[symbol]:
			self.n[symbol][table] += 1
		else:
			self.n[symbol][table] = 1
			if next_state == None:
				dish = crp(self.m,self.alpha_0)
			self.k[symbol][table] = dish
			if dish in self.m:
		       		self.m[dish] += 1
		       	else:
		       		self.m[dish] = 1
                
	def addpair(self,pair,next_state=None,next_table=None):
		self.add(pair[0],pair[1],next_state,next_table)
                
	def remove(self,state,symbol):
		if (state,symbol) in self.t:
			table = self.t[(state,symbol)]
			del self.t[(state,symbol)]
			self.n[symbol][table] -= 1
			if self.n[symbol][table] == 0:
				dish = self.k[symbol][table]
				del self.n[symbol][table]
				del self.k[symbol][table]
				self.m[dish] -= 1
				if self.m[dish] == 0:
					del self.m[dish]

	def removepair(self,pair):
		self.remove(pair[0],pair[1])

	def next(self,state,symbol,new_state=False): # if we wish to always create a new state for a new state/symbol pair (rather than having some chance of transitioning to an existing state) set the flag new_state=True
		if new_state == True:
			next_state = gensym(self.m)
		else:
			next_state = None
		if (state,symbol) not in self.t:
			self.add(state,symbol,next_state)
		return self.k[symbol][self.t[(state,symbol)]]

	def run(self,seq,start_state=0): # returns a generator that iterates over the states the machine traverses given a sequence (creating new states as need be)
		state = start_state
		for symbol in seq:
			yield (state,symbol)
			state = self.next(state,symbol)

	def generate(self,n,emission,start_state=0,end_state=None): # generates a length n sequence given emission probabilities for each state
		state = start_state
		if end_state == None:
			for i in range(n):
				symbol = discrete_sample(emission[state])
				state = self.next(state,symbol)
				yield symbol
		else:
			while state != end_state:
				symbol = discrete_sample(emission[state])
				state = self.next(state,symbol)
				yield symbol

	def generatefromcounts(self,n,counts,state_state=0): # integrates out the emission probabilities, so the counts get updated as we go along
		state = start_state
		for i in range(n):
			emission = [self.beta/self.S for i in range(self.S)]
			for j in counts:
				if j[0] == state:
					emission[j[1]] += counts[j]
			symbol = discrete_sample(emission)
			if (state,symbol) in counts:
				counts[(state,symbol)] += 1
			else:
				count[(state,symbol)] = 1
			state = self.next(state,symbol)
			yield symbol

	# Metropolis updates for all the hyperparameters with Gamma(1,1) priors
	def sample_alpha_0(self):
		alpha_0_new = random.gauss(self.alpha_0,0.1) # the proposal
		if alpha_0_new > 0:
			old_lik = len(self.m)*log(self.alpha_0) + log(self.alpha_0) - log(self.alpha_0 + sum(self.m.values())) - self.alpha_0
			new_lik = len(self.m)*log(alpha_0_new) + log(alpha_0_new) - log(alpha_0_new + sum(self.m.values())) - alpha_0_new
			if log(random.random()) < new_lik - old_lik:
				self.alpha_0 = alpha_0_new

	def sample_alpha(self):
		alpha_new = random.gauss(self.alpha,0.1) # the proposal
		if alpha_new > 0:
			old_lik = sum(len(self.k[symbol])*log(self.alpha) + lgamma(self.alpha) - lgamma(self.alpha + len([x for x in self.t if x[1] == symbol])) for symbol in range(self.S)) - self.alpha
			new_lik = sum(len(self.k[symbol])*log(alpha_new) + lgamma(alpha_new) - lgamma(alpha_new + len([x for x in self.t if x[1] == symbol])) for symbol in range(self.S)) - alpha_new
			if log(random.random()) < new_lik - old_lik: # accept the sample
				self.alpha = alpha_new

	def sample_beta(self,seq,old_score):
		beta_old = self.beta
		old_lik = old_score - beta_old
		self.beta = random.gauss(self.beta,0.1)
		if self.beta > 0:
			new_lik = self.scoreseq(seq) - self.beta
			if log(random.random()) > new_lik - old_lik: # reject the sample
				self.beta = beta_old
		else:
			self.beta = beta_old

	def state(self,seq,start_state=0): # returns the state at which the machine ends given the input seq
		for state,symbol in self.run(seq,start_state):
			pass
		return state

	def count(self,seq,prior_counts=None,start_state=0):
		if type(seq[0]) != list: # if we want to count only one sequence
			return self.count([seq],prior_counts,start_state)
		else:
			if prior_counts == None:
				counts = {}
			else:
				counts = prior_counts.copy()
			for subseq in seq:
				for i in self.run(subseq,start_state):
					if i in counts:
						counts[i] += 1
					else:
						counts[i] = 1
			return counts

	def state_counts(self,counts):
		state_counts = dict([(i,sum(counts[j] for j in counts if j[0] == i)) for i in self.m]) # the total number of times a state is visited
		state_counts[0] = sum(counts[j] for j in counts if j[0] == 0) # since state 0 is always transient, it won't appear in self.m, so add by hand
		for i in state_counts.copy():
			if state_counts[i] == 0:
				del state_counts[i]
		return state_counts

	def score(self,counts): # Returns the log likelihood of the sequence given the PDFA.  I should double-check for the particle filter case that it's ok to average these.
		s_counts = self.state_counts(counts)
		return sum(lgamma(counts[x] + self.beta/self.S) - lgamma(self.beta/self.S) for x in counts) - sum(lgamma(s_counts[y] + self.beta) - lgamma(self.beta) for y in s_counts)
        
	def particlescore(self,seq,n=1,prior_counts=None,start_state=0): # For n=1 gives the same answer as scoreseq.  Otherwise averages the probability of each symbol in a sequence over many particle samples
		if type(seq[0]) != list:
			return self.particlescore([seq],n,prior_counts,start_state)
		else:
			scores = []
			for subseq in seq:
				scores.append([0 for i in subseq])
			for i in range(n):
				if prior_counts == None:
					counts = {}
					state_counts = {}
				else:
					counts = prior_counts.copy()
					state_counts = dict([(i,sum(counts[j] for j in counts if j[0] == i)) for i in self.m])
					state_counts[0] = sum(counts[j] for j in counts if j[0] == 0)
				old_t = self.t.copy() # since scoring the sequence will create new state/symbol pairs, keep track of the new ones and remove them
				for j,subseq in enumerate(seq):
					for k,pair in enumerate(self.run(subseq,start_state)):
						state,symbol = pair
						if pair in counts:
       							scores[j][k] += (counts[pair] + self.beta/self.S)/(state_counts[state] + self.beta)/n
							counts[pair] += 1
							state_counts[state] += 1 # if this state/symbol pair has been observed, then this state has definitely been observed, no need to worry about initializing it
						else:
							counts[pair] = 1
							if state in state_counts:
								scores[j][k] += (self.beta/self.S)/(state_counts[state] + self.beta)/n
								state_counts[state] += 1
							else:
								scores[j][k] += 1/float(self.S)/n
								state_counts[state] = 1
				map(self.removepair,[x for x in self.t if x not in old_t]) # removes added state/symbol pairs from the pdfa
			return sum(sum(log(x) for x in y) for y in scores) # good lord this is crufty compared to the single-particle score


	def scoreseq(self,seq,prior_counts=None,start_state=0):
		return self.score(self.count(seq,prior_counts,start_state))

	def merge(self,state1,state2):
		if state2 == 0 and state1 != 0:
			self.determinize({},{state1:[0]})
		else:
			self.determinize({},{state2:[state1]})

	# to_merge maps a state to a list containing the state it is to be merged into.  all states merging into one state are mapped to the same list object, so we can change its value for all states at once.
	# nondeterm is a dictionary from state/symbol pairs to a list of states.  it contains all the transitions other than the primary one.  when it is empty for all pairs we are done merging. 
	def determinize(self,nondeterm,to_merge):
		start = True
		while start or len(nondeterm) > 0:
			start = False # after the first loop, if there's nothing in nondeterm it's finished
			unseen = set(to_merge.keys())
			tcopy = self.t.copy() # since python doesn't allow lists to change size while looping over them, have to loop over this instead
		       	for pair in tcopy:
				if pair in self.t:
					state = self.next(pair[0],pair[1])
					if state in to_merge:
						if state in unseen:
							unseen.remove(state)
						self.removepair(pair)
						self.addpair(pair,to_merge[state][0])
						if pair in nondeterm and to_merge[state][0] in nondeterm[pair]: 
							# if the state we are merging into is already in the list of nondeterministic transitions for this state/symbol pair, remove it now that it is the primary transition
							nondeterm[pair].remove(to_merge[state][0])
							if len(nondeterm[pair]) == 0:
								del nondeterm[pair]
					if pair in nondeterm:
						for state in nondeterm[pair].copy():
							if state in to_merge:
								if state in unseen:
									unseen.remove(state)
								nondeterm[pair].remove(state)
								if self.next(pair[0],pair[1]) != to_merge[state][0]:
									nondeterm[pair].add(to_merge[state][0])
								elif len(nondeterm[pair]) == 0:
									del nondeterm[pair]
					if pair[0] in to_merge:
						if pair[0] in unseen:
							unseen.remove(pair[0])
						newstate = to_merge[pair[0]][0]
						state = self.next(pair[0],pair[1])
						self.removepair(pair)
						if (newstate,pair[1]) in self.t:
							if self.next(newstate,pair[1]) != state:
								if (newstate,pair[1]) in nondeterm:
									if state not in nondeterm[(newstate,pair[1])]:
										nondeterm[(newstate,pair[1])].add(state)
										add_to_merge(self.next(newstate,pair[1]),state,to_merge)
								else:
									nondeterm[(newstate,pair[1])] = set([state])
									add_to_merge(self.next(newstate,pair[1]),state,to_merge)
						else:
							self.add(newstate,pair[1],state)
			for state in unseen:
				del to_merge[state]

# to_merge is a dictionary that maps a state to the state it is to be merged into.  
# specifically, it maps to a list containing the state, so that all keys pointing to the same value can have their value changed at once (lists are mutable, integers are not)
def add_to_merge(state1,state2,to_merge):
	if state1 == 0:
		add_to_merge_case(state1,state2,to_merge,0)
	elif state2 == 0:
		add_to_merge_case(state2,state1,to_merge,0)
	elif [state1] in to_merge.values():
		if [state2] in to_merge.values():
			add_to_merge_case(state1,state2,to_merge,3)
		elif state2 in to_merge.keys():
			add_to_merge_case(state1,state2,to_merge,2)
		else:
			add_to_merge_case(state1,state2,to_merge,0)
	elif [state2] in to_merge.values():
		if state1 in to_merge.keys():
			add_to_merge_case(state2,state1,to_merge,2)
		else:
			add_to_merge_case(state2,state1,to_merge,0)
	elif state1 in to_merge.keys():
		if state2 in to_merge.keys():
			add_to_merge_case(state2,state1,to_merge,4)
		else:
			add_to_merge_case(state1,state2,to_merge,1)
	elif state2 in to_merge.keys():
		add_to_merge_case(state2,state1,to_merge,1)
	else:
		add_to_merge_case(state1,state2,to_merge,0)

def add_to_merge_case(state1,state2,to_merge,case):
	if case == 0: # trivial case, state1 is a value or not in the dict, state2 is not in the dict
		to_merge[state2] = [state1]
	elif case == 1: # state1 is a key, state2 is not in the dict
       		to_merge[state2] = to_merge[state1]
	elif case == 2: # state1 is a value, state2 is a key
       		old_state = to_merge[state2][0]
       		to_merge[state2][0] = state1
       		to_merge[old_state] = to_merge[state2]
	elif case == 3: # state1 and state2 are values
		to_merge[state2] = [state1]
		idx = to_merge.keys()[to_merge.values().index([state2])]
		to_merge[idx][0] = state1 # crufty! but this finds a key that maps to the value [state2] and changes what's inside the list to state1
		to_merge[state2] = to_merge[idx]
	elif case == 4: # state1 and state2 are keys
		to_merge[to_merge[state2][0]] = to_merge[state1]
		to_merge[state2] = to_merge[state1]

def crp(rest,alpha):
       	prob = rest.copy()
       	prob[gensym(prob)] = alpha
       	return discrete_sample(prob)

def discrete_sample(p,normal=False):
	if not normal:
		if type(p) == dict:
			pval = p.values()
		elif type(p) == list:
			pval = p
		sample = sum(pval) * random.random()
	else:
		sample = random.random()
	for x in cumsum(p):
	       	if x[1] >= sample:
	       		return x[0]
	return None
    
def ngram(seq,n,a=1,a_0=1,b=1): # returns a pdfa initialized to the n-gram model for a given sequence
	if type(seq[0]) != list:
		return ngram([seq],n,a,a_0,b)
	else:
		pdfa = PDFA(max(max(subseq) for subseq in seq) + 1,a,a_0,b)
		context_to_state = {'deque([])':0} # initialize by mapping empty context to initial state
		for subseq in seq:
			context = deque([])
			state = 0
			for symbol in subseq:
				context.append(symbol)
				if len(context) >= n:
					context.popleft()
				if repr(context) not in context_to_state:
					new_state = gensym(pdfa.m.keys())
					pdfa.add(state,symbol,new_state)
					state = new_state
					context_to_state[repr(context)] = state
				else:
					state = context_to_state[repr(context)]
		return pdfa

def pta(seq,a=1,a_0=1,b=1): # returns the prefix tree acceptor for a set of strings
	if type(seq[0]) != list:
		return pta([seq],a,a_0,b)
	else:
		pdfa = PDFA(max(max(subseq) for subseq in seq) + 1,a,a_0,b)
		for subseq in seq:
			state = 0
			for symbol in subseq:
				state = pdfa.next(state,symbol,True)
		return pdfa

def even(n):
	pdfa = PDFA(2)
	pdfa.add(0,0,0)
	pdfa.add(0,1,1)
	pdfa.add(1,1,0)
	return pdfa.generate(n,{0:[1,1],1:[0,1]})

def sevenstate(n):
	pdfa = PDFA(2)
	transition = {(0,0):1,(0,1):4,(1,0):2,(1,1):5,(2,0):3,(2,1):6,(3,0):3,(3,1):0,(4,0):1,(4,1):4,(5,0):1,(5,1):4,(6,0):1,(6,1):4}
	emission = {0:[3,13],1:[9,7],2:[9,7],3:[3,13],4:[15,1],5:[1,3],6:[3,1]}
	map(pdfa.addpair,transition.keys(),transition.values())
	return pdfa.generate(n,emission)

def reber(n=0):
	pdfa = PDFA(7)
	transition = {(0,0):1,(1,1):2,(1,2):3,(2,3):2,(2,4):4,(3,1):3,(3,5):5,(4,4):3,(4,3):6,(5,5):6,(5,2):4}
	emission = {0:{0:1},1:{1:1,2:1},2:{3:3,4:2},3:{1:7,5:3},4:{4:1,3:1},5:{5:1,2:1},6:{6:1}}
	map(pdfa.addpair,transition.keys(),transition.values())
	if n != 0: # if there's no end state, just loop back around to the start
		pdfa.addpair((6,6),0)
		return pdfa.generate(n,emission)
	else: # if there is an end state, make it state #7
		pdfa.addpair((6,6),7)
		return pdfa.generate(n,emission,0,7)

def embedded(): # a 14-state grammar with 2 identical 6-state grammars embedded within it
	pdfa = PDFA(2)
	transition = {(0,0):1,(1,0):2,(1,1):8,(2,0):3,(2,1):4,(3,0):3,(3,1):5,(4,0):6,(4,1):4,(5,0):4,(5,1):7,(6,0):7,(6,1):5,(7,1):14,(8,0):9,(8,1):10,(9,0):9,(9,1):11,(10,0):12,(10,1):10,(11,0):10,(11,1):13,(12,0):13,(12,1):11,(13,0):14,(14,1):0}
	map(pdfa.addpair,transition.keys(),transition.values())
	return pdfa

def gen(generator):
	return [x for x in generator]

def cumsum(x):
	sum = 0
	if type(x) == dict:
		iter = x
	elif type(x) == list:
		iter = range(len(x))
	for i in iter:
		sum += x[i]
		yield (i,sum)

def gensym(x): # generates a random number not already in x
	while True:
		rand = random.randint(1,maxint)
		if rand not in x:
			return rand

# averages over many pdfas.  prior_counts is now a *list* of arrays mapping state/symbol pairs to counts
def avgscore(pdfas,seq,n=1,prior_counts=None,start_state=0): # For n=1 gives the same answer as scoreseq.  Otherwise averages the probability of each symbol in a sequence over many particle samples
	if type(seq[0]) != list:
		return avgscore(pdfas,[seq],n,prior_counts,start_state)
	else:
		if start_state == 0:
			ss = [0 for x in range(len(pdfas))]
		else:
			ss = start_state
		scores = []
		for subseq in seq:
			scores.append([0 for i in subseq])
		for i in range(n):
			if prior_counts == None:
				counts = [{} for x in range(len(pdfas))]
				state_counts = [{} for x in range(len(pdfas))]
			else:
				counts = prior_counts
				state_counts = [dict([(i,sum(count[j] for j in count if j[0] == i)) for i in pdfas[k].m]) for k,count in enumerate(counts)]
				for i,count in enumerate(counts):
					state_counts[i][0] = sum(count[j] for j in count if j[0] == 0)
			for s,pdfa in enumerate(pdfas):
				old_t = pdfa.t.copy() # since scoring the sequence will create new state/symbol pairs, keep track of the new ones and remove them
				for j,subseq in enumerate(seq):
					for k,pair in enumerate(pdfa.run(subseq,ss[s])):
						state,symbol = pair
						if pair in counts[s]:
   							scores[j][k] += (counts[s][pair] + pdfa.beta/pdfa.S)/(state_counts[s][state] + pdfa.beta)/n/len(pdfas)
							counts[s][pair] += 1
							state_counts[s][state] += 1 # if this state/symbol pair has been observed, then this state has definitely been observed, no need to worry about initializing it
						else:
							counts[s][pair] = 1
							if state in state_counts[s]:
								scores[j][k] += (pdfa.beta/pdfa.S)/(state_counts[s][state] + pdfa.beta)/n/len(pdfas)
								state_counts[s][state] += 1
							else:
								scores[j][k] += 1/float(pdfa.S)/n/len(pdfas)
								state_counts[s][state] = 1
				map(pdfa.removepair,[x for x in pdfa.t if x not in old_t]) # removes added state/symbol pairs from the pdfa
		return sum(sum(log(x) for x in y) for y in scores) # good lord this is crufty compared to the single-particle score

