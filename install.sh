#!/bin/sh
set -e

# Install script to install jpx

version=`curl --silent https://api.github.com/repos/kppk/jpx/releases/latest  | grep tag_name | cut -f 2 -d : | cut -f 2 -d '"'`

command_exists() {
  command -v "$@" > /dev/null 2>&1
}

case "$(uname -m)" in
  *64)
    ;;
  *)
    echo >&2 'Error: you are not using a 64bit platform.'
    echo >&2 'JPX currently only supports 64bit platforms.'
    exit 1
    ;;
esac

user="$(id -un 2>/dev/null || true)"

sh_c='sh -c'

curl=''
if command_exists curl; then
  curl='curl -sSL -o'
else
    echo >&2 'Error: this installer needs the ability to run curl.'
    echo >&2 'We are unable to find "curl" available to make this happen.'
    exit 1
fi

url='https://github.com/kppk/jpx/releases/download'

# perform some very rudimentary platform detection
case "$(uname)" in
  Linux)
    echo "Linux support comming soon..."
    exit 1
    ;;
  Darwin)
    $sh_c "$curl /tmp/jpx_mac $url/$version/jpx_mac"
    $sh_c "mkdir -p  ~/.jpx/bin"
    $sh_c "mv /tmp/jpx_mac ~/.jpx/bin/jpx"
    $sh_c "chmod +x ~/.jpx/bin/jpx"
    echo "Add `~/.jpx/bin` directory to your PATH"
    echo "DONE"
    ;;
  WindowsNT)
    echo "Windows support coming soon..."
    exit 1
    ;;
  *)
    cat >&2 <<'EOF'

  Either your platform is not easily detectable or is not supported by this
  installer script (yet - PRs welcome! [kppk/jpx]).
  Please visit the following URL for more detailed installation instructions:

    https://github.com/kppk/jpx

EOF
    exit 1
esac

exit 0