#!/bin/bash

BIN=$(dirname $0)

CONTROLLER=127.0.0.1

function check_controller
{
    RESULT=$($BIN/client-cli.sh --connect $CONTROLLER --root --scripting --cmd "status" 2> /dev/null)
    if echo $RESULT | grep -s '"status"\s*:\s*"AVAILABLE"' > /dev/null; then
        echo "OK (controller available at '$CONTROLLER' for domain '`echo $RESULT | sed -s 's/^.\+"name" : "\([^"]\+\)".\+$/\1/'`')"
        exit 0
    else
        echo "FAILED (controller not available at '$CONTROLLER')"
        exit 2
    fi
}

function check_connector
{
    AGENTS=$(echo $1 | sed -s 's/,/ /g')
    RESULT=$($BIN/client-cli.sh --connect $CONTROLLER --root --scripting --cmd "list-connectors" 2> /dev/null)
    if [ -z "$RESULT" ]; then
        echo "FAILED (controller not available at '$CONTROLLER')"
        exit 2
    fi
    RESULT=$(echo $RESULT | sed -s 's/}, {/\n/g' | sed -s 's/^.\+"status" : "\([^"]\+\)".\+"agent" : "\([^"]\+\)".\+$/\2=\1/')
    AGENTS_NOT_EXIST=""
    AGENTS_NOT_AVAILABLE=""
    AGENTS_AVAILABLE=""
    for AGENT in $AGENTS
    do
        if echo $RESULT | grep "$AGENT" > /dev/null; then
            STATUS=$(echo $RESULT | sed "s/^.*$AGENT=\([^ ]\+\).\+\$/\1/g")
            if [ "$STATUS" = "AVAILABLE" ]; then
                if [ -n "$AGENTS_AVAILABLE" ]; then
                    AGENTS_AVAILABLE="$AGENTS_AVAILABLE, "
                fi
                AGENTS_AVAILABLE="$AGENTS_AVAILABLE$AGENT"
            else
                if [ -n "$AGENTS_NOT_AVAILABLE" ]; then
                    AGENTS_NOT_AVAILABLE="$AGENTS_NOT_AVAILABLE, "
                fi
                AGENTS_NOT_AVAILABLE="$AGENTS_NOT_AVAILABLE$AGENT"
            fi
        else
            if [ -n "$AGENTS_NOT_EXIST" ]; then
                AGENTS_NOT_EXIST="$AGENTS_NOT_EXIST, "
            fi
            AGENTS_NOT_EXIST="$AGENTS_NOT_EXIST$AGENT"
        fi
    done
    if [ -z "$AGENTS_NOT_EXIST" ] && [ -z "$AGENTS_NOT_AVAILABLE" ]; then
        echo "OK (available: [$AGENTS_AVAILABLE])"
        exit 0
    else
        echo "FAILED (available: [$AGENTS_AVAILABLE]; not-available: [$AGENTS_NOT_AVAILABLE]; not-exist: [$AGENTS_NOT_EXIST])"
        exit 2
    fi
}

function check_client_web
{
    echo TODO: check client web
    exit 3
}

OPTIND=0
while getopts "c:" option; do
case $option in
    c)
        CONTROLLER=$OPTARG
        ;;
    esac
done
shift $(($OPTIND - 1))

case $1 in
    connector)
        check_connector $2
        ;;
    client_web)
        check_client_web
        ;;
    *)
        check_controller
        ;;
esac