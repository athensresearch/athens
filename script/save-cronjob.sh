#!/bin/bash

:  <<'END_COMMENT'
 For cron:

   Put a shell script in one of these folders:
   /etc/cron.daily, /etc/cron.hourly, /etc/cron.monthly or /etc/cron.weekly.

   If these are not enough for you, you can add more specific tasks e.g. twice a month or every 5 minutes. Go to the terminal and type:

   `crontab -e`

   This will open your personal crontab (cron configuration file). The first line in that file explains it all! In every line you can
   define one command to run and its schedule, and the format is quite simple when you get the hang of it. The structure is:
     `minute hour day-of-month month day-of-week command`

   For all the numbers you can use lists, e.g. 5,34,55 in the minutes field will mean run at 5 past, 34 past, and 55 past whatever hour is defined.
   You can also use intervals. They are defined like this: */20. This example means every 20th, so in the minutes column it is equivalent to 0,20,40.

   So to run a command every Monday at 5:30 in the afternoon:
    `30 17 * * 1 /path/to/command`

   or every 15 minutes
    `*/15 * * * * /path/to/command`

   Note 1: that the day-of-week goes from 0-6 where 0 is Sunday.
   Note 2: These changes are applied automatically, you don't need to restart/reload anything.
END_COMMENT


# Following is the script that is needed for the cron

# This is the default location to save the ledger backups, modify it to change the default location.
# Filesystem Hierarchy https://www.pathname.com/fhs/pub/fhs-2.3.html


FOLDER=/var/lib/athens/backups/

if [ ! -d "$FOLDER" ]; then
  mkdir -p "$FOLDER"
fi

# Some of the strategies to calculate the filename
#  - Timestamp
#  - Monotonically increasing no.
#  - No. of files in folder + 1

# Going with the Timestamp option
TIMESTAMP=$(date +%F-%H-%M)
FILENAME="${FOLDER}${TIMESTAMP}.edn"

# If java not installed then install it

if [ -z "$(which java)" ]; then
  echo "Java is not installed on your operating system, please install it and try again"
  exit 1
fi

# command to save ledger
java -jar ~/athens-cli.jar save -f "$FILENAME"
