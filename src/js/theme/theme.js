import { extendTheme } from '@chakra-ui/react'
import { red } from '@material-ui/core/colors'
import { spacing } from './spacing'


const colors = {
  // Old theme values

  // light theme
  linkLight: "#0071DB",
  highlightLight: "#F9A132",
  textHighlightLight: "#ffdb8a",
  warningLight: "#D20000",

  backgroundPlus2Light: "#fff",
  backgroundPlus1Light: "#fbfbfb",
  backgroundColorLight: "#F6F6F6",
  backgroundMinus1Light: "#FAF8F6",
  backgroundMinus2Light: "#EFEDEB",

  confirmationLight: "#009E23",
  headerLight: "#322F38",
  bodyLight: "#433F38",
  borderLight: "hsla(32, 81%, 10:  0.08)",
  errorLight: "#fd5243",
  shadowLight: "#000",

  // dark theme
  backgroundMinu2Dark: "#151515",
  backgroundMinus1Dark: "#111",
  backgroundColorDark: "#1A1A1A",
  backgroundPlus1Dark: "#222",
  backgroundPlus2Dark: "#333",

  linkDark: "#498eda",
  highlightDark: "#FBBE63",
  textHighlightDark: "#FBBE63",
  warningDark: "#DE3C21",

  confirmationDark: "#189E36",
  headerDark: "#BABABA",
  bodyDark: "#AAA",
  borderDark: "hsla(32, 81%, 90%, 0.08)",
  errorDark: "#fd5243",
  shadowDark: "#000"
}

const semanticTokens = {
  colors: {
    transparent: 'transparent',
    brand: {
      default: 'linkLight',
      _dark: 'linkDark'
    },
    // separator colors
    "separator.border": {
      default: 'borderLight',
      _dark: 'borderDark'
    },
    "separator.divider": {
      default: 'borderLight',
      _dark: 'borderDark'
    },
    // background colors
    "background.floor": {
      default: 'backgroundColorLight',
      _dark: 'backgroundColorDark'
    },
    "background.basement": {
      default: 'backgroundMinus1Light',
      _dark: 'backgroundMinus1Dark'
    },
    "background.upper": {
      default: 'backgroundPlus1Light',
      _dark: 'backgroundPlus1Dark'
    },
    "background.attic": {
      default: 'backgroundPlus2Light',
      _dark: 'backgroundPlus2Dark'
    },
    // foreground colors
    "foreground.primary": {
      default: 'bodyLight',
      _dark: 'bodyDark'
    },
    "foreground.secondary": {
      default: 'headerLight',
      _dark: 'headerDark'
    },
    // status colors
    error: {
      default: 'errorLight',
      _dark: 'errorDark'
    },
    warning: {
      default: 'warningLight',
      _dark: 'warningDark'
    },
    confirmation: {
      default: 'confirmationLight',
      _dark: 'confirmationDark'
    },
    // other colors
    headerText: {
      default: 'headerLight',
      _dark: 'headerDark'
    },
    textHighlight: {
      default: 'textHighlightLight',
      _dark: 'textHighlightDark'
    },
    highlight: {
      default: 'highlightLight',
      _dark: 'highlightDark'
    },
    link: {
      default: 'linkLight',
      _dark: 'linkDark'
    }
  }
}

const components = {
  Popover: {
    baseStyle: {
      content: {
        bg: 'background.upper',
        shadow: '0 0.5rem 1rem rgba(0, 0, 0, 0.15)'
      }
    }
  },
  Modal: {
    baseStyle: {
      dialogContainer: {
        _focus: {
          outline: 'none'
        }
      },
      dialog: {
        bg: 'background.upper'
      }
    }
  },
  Button: {
    baseStyle: {
      color: 'feature.primary',
    }
  },
  Tooltip: {
    baseStyle: {
      bg: 'feature.primary',
      px: "8px",
      py: "2px",
      borderRadius: "sm",
      fontWeight: "medium",
      fontSize: "sm",
      boxShadow: "md",
      maxW: "320px",
      zIndex: "tooltip",
    }
  },
  Table: {
    baseStyle: {
      border: 'separator.divider',
    },
    variants: {
      striped: {
        td: {
          border: 'none',
        },
        thead: {
          th: {
            border: 'none'
          }
        },
        tbody: {
          tr: {
            "&:nth-of-type(odd)": {
              td: {
                background: "background.upper",
                "&:first-of-type": {
                  borderLeftRadius: "lg"
                },
                "&:last-of-type": {
                  borderRightRadius: "lg"
                }
              },
            },
          },
        },
      }
    }
  }
}

const config = {
  initialColorMode: 'dark',
  useSystemColorMode: true,
}

const styles = {
  global: {
    'html, body': {
      bg: 'background.floor',
      color: 'foreground.primary',
    }
  }
}

const sizes = {
  ...spacing
}

export const theme = extendTheme({ config, colors, semanticTokens, spacing, sizes, components, styles });
