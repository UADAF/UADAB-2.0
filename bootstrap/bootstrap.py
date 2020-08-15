#!/usr/bin/env python3

import os
import sys
import subprocess

from sys import argv
from os.path import exists

def write_pid_file(pid):
    with open("pidfile", "w+") as pidfile:
        pidfile.write(str(pid))

def remove_pid_file():
    if exists("pidfile"):
        os.remove("pidfile")

if len(argv) < 2:
    print("Not enough arguments. You need to specify bot location")
    sys.exit(1)

if not exists(argv[1]):
    print(f"File {argv[1]} does not exists")
    sys.exit(2)

bot = argv[1]
with open("bot.log", "w+b") as bot_out:
    with subprocess.Popen(["java", "-jar", bot], stdout=bot_out, stderr=bot_out) as process:
        print("process started with pid", process.pid)
        write_pid_file(process.pid)
        try:
            process.wait()
        except KeyboardInterrupt:
            print("bootstrap has been killed by keyboard interrupt, stopping bot...")
        print("process has been terminated with code", process.returncode)
        remove_pid_file()
