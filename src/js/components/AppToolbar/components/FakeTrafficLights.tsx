import { Box, HStack } from '@chakra-ui/react';

const light = <Box
  as="span"
  borderRadius="full"
  bg="foreground.tertiary"
  opacity={0.5}
  height="12.5px"
  width="12.5px"
/>

export const FakeTrafficLights = (props) => {
  return <HStack spacing="7.5px" px="3.5px" {...props}>
    {light}
    {light}
    {light}
  </HStack>
}