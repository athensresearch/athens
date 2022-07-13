import React from 'react';
import { Box, Text, HStack, Textarea, Button } from '@chakra-ui/react'
import { ChatFilledIcon } from '@/Icons/Icons'

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
    <ChatFilledIcon gridArea="main" transform="scale(1.5) translateY(5%)" zIndex={0} />
    <Text zIndex={1} gridArea="main" color="background.basement" fontSize="xs">{formatCount(count)}</Text>
  </Box>
}