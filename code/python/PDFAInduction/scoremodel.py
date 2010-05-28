from special import lgamma

def scoremodel(machine):
    """
    Returns the likelihood of a machine given its own hyperparameters
    """
    score = len(machine.m)*log(machine.alpha_0) + sum(lgamma(x) for x in machine.m.values()) + lgamma(machine.alpha_0) - lgamma(machine.alpha_0 + sum(machine.m.values())) # score of the top-level seating arrangement
    for i in range(machine.S):
        score += len(machine.n[i])*log(machine.alpha) + sum(lgamma(x) for x in machine.n[i].values()) + lgamma(machine.alpha) - lgamma(machine.alpha + sum(machine.n[i].values()))
    return score
