import { Box, Input } from '@chakra-ui/react';

export const BlockFormInput = ({ children }) => {
  return (
    <Input
      as={Box}
      position="relative"
      size="sm"
      _focusWithin={{
        shadow: "focus"
      }}
      sx={{
        ".block-content": {
          gridArea: "unset",
          minHeight: "unset"
        },
        ".chakra-avatar__group": {
          position: "absolute",
          right: 0.5,
          top: 0.5
        }
      }}
    >
      {children}
    </Input>
  );
}