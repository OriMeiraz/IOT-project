from scipy.signal import find_peaks
lst = []
kernel = [2**-i for i in range(5, 0, -1)]

def add_accel(a):
    lst.append(-float(a))
    if len(lst)>5:
        idx,_ = find_peaks(lst[-5:], height=1)
        if len(idx) >=1 and idx[-1] == 3:
            return str(lst[3])
    return "False"




def clear():
    lst.clear()
