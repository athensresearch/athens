import React from 'react';
import { Box, Text, HStack, Textarea, Button, MenuItem, MenuGroup } from '@chakra-ui/react'
import { ContextMenuContext } from '@/App/ContextMenuContext';
import { withErrorBoundary } from "react-error-boundary";
import { Anchor } from '@/Block/Anchor';

interface InlineCommentInputProps {
  onSubmitComment: (comment: string) => void
}

const sanitizeCommentString = (comment: string) => comment.trim().replace(/\n/g, ' ')

export const InlineCommentInput = ({ onSubmitComment }: InlineCommentInputProps) => {
  const [commentString, setCommentString] = React.useState('');
  const textareaRef = React.useRef(null);

  const handleSubmitComment = (e) => {
    e.preventDefault();
    onSubmitComment(sanitizeCommentString(commentString));
    setCommentString('');
    textareaRef.current.value = '';
    textareaRef.current.focus();
  }

  return (<HStack align="stretch" _first={{ mt: 4 }}>
    <Textarea
      placeholder="Comment"
      defaultValue={commentString}
      m={0}
      variant="filled"
      ref={textareaRef}
      onChange={(e) => setCommentString(e.target.value)}
      onKeyDown={(e) => {
        // If pressing enter (but not also shift)
        if (e.key === 'Enter' && !e.shiftKey) {
          handleSubmitComment(e)
        }
      }}
    />
    <Button
      height="auto"
      onClick={(e) => handleSubmitComment(e)}
      isDisabled={!commentString}
    >Send</Button>
  </HStack>)
}

export const CommentAnchor = ({ menu, ...boxProps }) => {
  const ref = React.useRef();
  const { addToContextMenu, getIsMenuOpen } = React.useContext(ContextMenuContext);
  const isMenuOpen = getIsMenuOpen(ref);

  const MenuItems = () => {
    return menu
  }

  return <Anchor
    isActive={isMenuOpen}
    ref={ref}
    onClick={(event) => {
      addToContextMenu({ event, ref, component: MenuItems, anchorEl: ref, key: "comment" })
    }}
    {...boxProps}
  />
}

const CommentErrorMessage = () => <Text color="foreground.secondary" display="block" p={2} borderRadius="sm">Couldn't show this comment</Text>;

export const CommentContainer = withErrorBoundary(({ children, menu, isFollowUp }) => {
  const ref = React.useRef();
  const { addToContextMenu, getIsMenuOpen } = React.useContext(ContextMenuContext);
  const isMenuOpen = getIsMenuOpen(ref);

  const MenuItems = () => {
    return menu
  }

  return <Box
    ref={ref}
    bg={isMenuOpen ? "interaction.surface.hover" : "undefined"}
    borderRadius="sm"
    transitionProperty="colors"
    transitionDuration="fastest"
    m={-1}
    p={1}
    mt={isFollowUp ? 0 : 2}
    alignItems="stretch"
    justifyContent="stretch"
    rowGap={0}
    columnGap={2}
    display="grid"
    gridTemplateColumns="auto 1fr"
    gridTemplateRows="auto auto"
    gridTemplateAreas={`
    'byline byline byline'
    'anchor comment refs'
    '_ reactions reactions'`}
    onContextMenu={(event) => {
      addToContextMenu({ event, ref, component: MenuItems, key: "comment", isExclusive: true })
    }}
    _first={{
      borderTopWidth: 0
    }}
    sx={{
      "> button.anchor:not([data-active])": {
        color: "foreground.tertiary"
      },
      ":hover > button.anchor": {
        color: "foreground.secondary"
      }
    }}
  >
    {children}
  </Box>
}, { fallback: <CommentErrorMessage /> });
