l = "1010110011100001"
history = { l }

for i in range(1 << 16):
    nxt_l = ["_"] * 16
    for j in range(1, 16):
        nxt_l[j] = l[j - 1]
    nxt_l[0] = f"{int(l[0]) ^ int(l[10]) ^ int(l[12]) ^ int(l[13]) ^ int(l[15])}"
    nxt_l = "".join(nxt_l)
    if nxt_l in history:
        print(f"Same: {nxt_l}")
        break
    