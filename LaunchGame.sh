#!/bin/bash

number_instances=$1
if [ -z "$1" ] || [ "$1" -lt 1 ]; then
	number_instances=2
fi

for ((i = 0; i < number_instances; i++)) do
	x-terminal-emulator -e ./build/bin/linux/debugExecutable/Quantum-Go-Fish.kexe
done
