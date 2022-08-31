import { Box, Input, Textarea } from '@chakra-ui/react';

export const BlockFormInput = ({ children, isMultiline, ...inputProps }) => {
  const Component = isMultiline ? Textarea : Input;

  return (
    <Component
      as={Box}
      position="relative"
      height={isMultiline ? 'auto' : undefined}
      py={1}
      {...inputProps}
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
    </Component>
  );
}