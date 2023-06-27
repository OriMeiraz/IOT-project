from scipy.signal import find_peaks
lst = []
def main(N):
    lst.append(float(N))
    peaks, _ = find_peaks(lst, threshold=1.8, distance=3)
    return str(len(peaks))

def clear():
    lst.clear()
