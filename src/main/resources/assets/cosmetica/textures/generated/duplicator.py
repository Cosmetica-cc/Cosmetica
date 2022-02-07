import shutil, os

# ==== VARIABLES ====
SRC = "./reserved_0.png"
DEST = "./reserved_"
N_COUNT = 128
# ===================

for i in range(1, N_COUNT):
    shutil.copyfile(SRC, DEST + str(i) + ".png")

