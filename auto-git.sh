#!/usr/bin/expect -f

set argsCount [llength $argv]
lassign $argv arg1 arg2 arg3

set timeout 15

if { $argsCount == 1} {
    spawn git $arg1 
} elseif { $argsCount == 2} {
    spawn git $arg1 $arg2
} elseif { $argsCount == 3} {
    spawn git $arg1 $arg2 $arg3
} else {
    send_user "expected at least 1 argument\n"
    exit
}

expect -re 'Username for ' {
    send -- "${env(GIT_REPOSITORY_USERNAME)}\n"
}

expect -re "Password for " {
    send -- "${env(GIT_REPOSITORY_TOKEN)}\n"
}

expect eof