#!/bin/bash
# curl https://afzalex.site/scripts/install-flog.sh | /bin/bash

GITCONFIG_LOC="$HOME/.gitconfig"

RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m' # No Color

if ! cat "$GITCONFIG_LOC" | grep '#### FZ_CODE_FLOG_INSTALLATION ####' > /dev/null
then 
cat <<END >> $GITCONFIG_LOC

#### FZ_CODE_FLOG_INSTALLATION ####
[alias]
    flog = log --graph --abbrev-commit --decorate --format=format:'%C(bold blue)%h%C(reset) - %C(bold green)(%ar)%C(reset) %C(white)%s%C(reset) %C(dim white)- %an%C(reset)%C(bold yellow)%d%C(reset)' --all
END
    printf "${GREEN}flog installed successfully${NC}\n"
else
    printf "${GREEN}flog is already installed${NC}\n"
fi