import random
from sys import maxint
from math import log
from special import lgamma
from collections import deque

def mh_sample(seq,num_samples,a,a_0,b,init=None):
	if init == None:
		num_symbols = max(seq) + 1
		old_machine = PDFA(num_symbols,a,a_0,b)
		pdfas = []
	else:
		old_machine = init.deepcopy()
		pdfas = [init]
	scores = [old_machine.scoreseq(seq)]
	numstates = [old_machine.numstates()]
	for t in range(num_samples):
		print 'Sweep:', t, 'out of', num_samples, '\n\t', numstates[-1], 'states'
		machine = old_machine.deepcopy()
		for pair in old_machine.t:
			if pair in machine.t:
				for i in range(10): # sample multiple possible assignments for this state/symbol transition given the rest of the transitions
					new_machine = machine.deepcopy()
					new_machine.removepair(pair)
					counts = new_machine.count(seq) # since only pair has been changed, the sequence must visit this pair at least once and pick a new following state
					score = new_machine.score(counts)
					if log(random.random()) < score - scores[-1]: # accept the sample
						machine = new_machine
						map(machine.removepair,[x for x in counts if counts[x] == 0])
						scores.append(score)
					else:
						scores.append(scores[-1])
					numstates.append(machine.numstates())
		if t % 10 == 9:
			pdfas.append(machine)
		old_machine = machine
	return [pdfas, scores, numstates]

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
				prob = dict([(i,self.n[symbol][i]) for i in self.n[symbol] if self.k[symbol][i] != next_state]) # counts the number of times a customer sits at a table serving the dish next_state
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

	def next(self,state,symbol):
		if (state,symbol) not in self.t:
			self.add(state,symbol)
		return self.k[symbol][self.t[(state,symbol)]]

	def run(self,seq,start_state=0): # returns a generator that iterates over the states the machine traverses given a sequence (creating new states as need be)
		state = start_state
		for symbol in seq:
			yield (state,symbol)
			state = self.next(state,symbol)

	def generate(self,n,emission,start_state=0): # generates a length n sequence given emission probabilities for each state
		state = start_state
		for i in range(n):
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

	def count(self,seq,start_state=0):
		counts = {}
		for i in self.run(seq,start_state):
			if i in counts:
				counts[i] += 1
			else:
				counts[i] = 1
		return counts

	def score(self,counts): # Returns the log likelihood of the sequence given the PDFA.  I should double-check for the particle filter case that it's ok to average these.
		state_counts = dict([(i,sum(counts[j] for j in counts if j[0] == i)) for i in self.m]) # the total number of times a state is visited
		return sum(lgamma(counts[x] + self.beta/self.S) - lgamma(self.beta/self.S) for x in counts) - sum(lgamma(state_counts[y] + self.beta) - lgamma(self.beta) for y in state_counts)
        
	def scoreseq(self,seq,start_state=0):
		return self.score(self.count(seq,start_state))

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
    
def ngram(seq,n,a,a_0,b): # returns a pdfa initialized to the n-gram model for a given sequence
	pdfa = PDFA(max(seq) + 1,a,a_0,b)
	context = deque([])
	state = 0
	context_to_state = {'deque([])':0} # initialize by mapping empty context to initial state
	for symbol in seq:
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

def reber(n):
	pdfa = PDFA(7)
	transition = {(0,0):1,(1,1):2,(1,2):3,(2,3):2,(2,4):4,(3,1):3,(3,5):5,(4,4):3,(4,3):6,(5,5):6,(5,2):4,(6,6):0}
	emission = {0:{0:1},1:{1:1,2:1},2:{3:3,4:2},3:{1:7,5:3},4:{4:1,3:1},5:{5:1,2:1},6:{6:1}}
	map(pdfa.addpair,transition.keys(),transition.values())
	return pdfa.generate(n,emission)

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
