import React from 'react';
import { Box, Text, HStack, Textarea, Button, MenuList, MenuItem } from '@chakra-ui/react'
import { ChatBubbleFillIcon } from '@/Icons/Icons'
// import { useOldContextMenu } from '@/utils/useContextMenu';
import { withErrorBoundary } from "react-error-boundary";

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

const formatCount = (count: number): string => {
  if (count > 9) return "9+"
  else if (count === 0) return ""
  else return count.toString()
};

export const CommentCounter = ({ count }) => {
  return <Box display="grid" gridTemplateAreas="'main'">
    <ChatBubbleFillIcon gridArea="main" zIndex={0} />
    <Text zIndex={1} gridArea="main" transform="translateY(5%)" color="background.basement" fontSize="xs">{formatCount(count)}</Text>
  </Box>
}

const CommentErrorMessage = () => <Text color="foreground.secondary" display="block" p={2} borderRadius="sm">Couldn't show this comment</Text>;

export const CommentContainer = withErrorBoundary(({ children, menu, isFollowUp }) => {
  const commentRef = React.useRef();

  // const {
  //   menuSourceProps,
  //   ContextMenu,
  //   isOpen: isContextMenuOpen
  // } = useContextMenu({
  //   ref: commentRef,
  //   source: "cursor",
  // });

  const menuList = React.useMemo(() => {
    return <MenuList>{menu.map((action) => <MenuItem key={action.children} {...action} />)}</MenuList>
  }, [menu])

  return <Box
    ref={commentRef}
    // {...menuSourceProps}
    // bg={isContextMenuOpen ? "interaction.surface.hover" : undefined}
    // borderRadius={isContextMenuOpen ? "sm" : undefined}
    mb="-1px"
    borderTop={isFollowUp ? null : "1px solid"}
    borderTopColor="separator.divider"
    alignItems="stretch"
    justifyContent="stretch"
    rowGap={0}
    columnGap={2}
    display="grid"
    gridTemplateColumns="auto 1fr"
    gridTemplateRows="auto auto"
    gridTemplateAreas={`
    'byline byline byline'
    'anchor comment refs'`}
    sx={{
      "> button.anchor:not([data-active])": {
        color: "foreground.tertiary"
      },
      ":hover > button.anchor": {
        color: "foreground.secondary"
      }
    }}
  >{children}
    {/* <ContextMenu>
      {menuList}
    </ContextMenu> */}
  </Box>
}, { fallback: <CommentErrorMessage /> });