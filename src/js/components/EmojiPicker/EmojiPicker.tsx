import React from 'react'
import 'emoji-picker-element';
import {
  Box,
  useTheme,
  useColorMode,
  Button,
  Popover,
  PopoverTrigger,
  Portal,
  PopoverBody,
  PopoverContent,
  useDisclosure,
} from "@chakra-ui/react";
import { PlusIcon } from '@/Icons/Icons'

// Workaround for https://github.com/nolanlawson/emoji-picker-element/issues/220
// Without this, the console gets spammed with uncaught errors when editing blocks.
// I can reproduce it by creating several blocks, then clicking on the dev console, then
// clicking on a block.
// This is a bad workaround because it might cause it to ignore other IDB errors, but
// we don't use IDB anywhere else for now.
window.addEventListener("unhandledrejection", event => {
  if (event.reason.message == "Failed to execute 'transaction' on 'IDBDatabase': The database connection is closing.") {
    event.preventDefault();
  }
});


interface EmojiPickerProps {
  onEmojiSelected: (event) => void;
}

const colorForMode = (mode, token) => mode === 'light' ? token.default : token._dark;

export const EmojiPicker = ({ onEmojiSelected }: EmojiPickerProps) => {
  const theme = useTheme()
  const ref = React.useRef(null)
  const { colorMode } = useColorMode()

  React.useEffect(() => {
    ref.current.addEventListener('emoji-click', event => {
      onEmojiSelected(event)
    })
    ref.current.skinToneEmoji = '👍'
  }, [])

  const colors = theme.semanticTokens.colors;
  const shadows = theme.semanticTokens.shadows;

  return <Box
    borderRadius="md"
    overflow="hidden"
    width="max-content"
    sx={{
      "emoji-picker": {
        "--background": colorForMode(colorMode, colors["background.attic"]), // Background of the entire <emoji-picker>

        "--border-color": colorForMode(colorMode, colors["separator.divider"]),
        "--border-size": "1px", // Width of border used in most of the picker

        "--button-active-background": colorForMode(colorMode, colors["interaction.surface.active"]), // Background of an active button
        "--button-hover-background": colorForMode(colorMode, colors["interaction.surface.hover"]), // Background of a hovered button

        "--category-emoji-padding": "var(--emoji-padding)", // Vertical / horizontal padding on category emoji, if you want it to be different from--emoji - padding
        "--category-emoji-size": "var(--emoji-size)", // Width / height of category emoji, if you want it to be different from--emoji - size
        "--category-font-color": colorForMode(colorMode, colors["foreground.secondary"]),	// Font color of custom emoji category headings
        "--category-font-size": "1rem", // Font size of custom emoji category headings

        "--emoji-padding": "0.5rem", // Vertical and horizontal padding on emoji
        "--emoji-size": "1.375rem", // Width and height of all emoji

        "--indicator-color": colorForMode(colorMode, colors["link"]),	// Color of the nav indicator
        "--indicator-height": "3px", // Height of the nav indicator

        "--input-border-color": colorForMode(colorMode, colors["separator.divider"]),
        "--input-border-radius": "0.5rem",
        "--input-border-size": "1px",
        "--input-font-color": colorForMode(colorMode, colors["foreground.primary"]),
        "--input-font-size": "1rem",
        "--input-line-height": "1.5",
        "--input-padding": "0.25rem",
        "--input-placeholder-color": colorForMode(colorMode, colors["foreground.secondary"]),

        "--num-columns": "8", // How many columns to display in the emoji grid

        "--outline-color": colorForMode(colorMode, shadows["focus"]),	// Focus outline color
        "--outline-size": "2px", // Focus outline width

        "--skintone-border-radius": "1rem" // Border radius of the skintone dropdown
      }
    }}
  >{React.createElement('emoji-picker', { ref })}</Box>;
}

interface EmojiPickerPopoverProps extends EmojiPickerProps {
  buttonLabel?: React.ReactElement;
}

export const EmojiPickerPopoverContent = ({ onEmojiSelected, onClose }) => {
  return <Portal>
    <PopoverContent minWidth="max-content">
      <PopoverBody p={0} >
        <EmojiPicker onEmojiSelected={(event) => {
          onEmojiSelected(event);
          onClose && onClose();
        }} />
      </PopoverBody>
    </PopoverContent>
  </Portal>
}

export const EmojiPickerPopover = ({ onEmojiSelected, buttonLabel = <PlusIcon /> }: EmojiPickerPopoverProps) => {
  const { isOpen, onToggle, onClose } = useDisclosure()

  return <Popover
    returnFocusOnClose={false}
    closeOnBlur={false}
    isOpen={isOpen}
    onClose={onClose}
  >
    <PopoverTrigger>
      <Button onClick={onToggle}>{buttonLabel}</Button>
    </PopoverTrigger>
    <EmojiPickerPopoverContent onEmojiSelected={onEmojiSelected} onClose={onClose} />
  </Popover>
}
