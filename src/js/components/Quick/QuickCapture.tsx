import React from 'react'
import { FormControl, Button, Box, HStack, Text, Textarea, VStack, Flex, FormLabel } from "@chakra-ui/react"
import { LayoutContext } from '@/Layout/useLayoutState'

const FloatingInput = (props) => {
  const { onSubmit } = props
  const [string, setString] = React.useState("")
  const inputRef = React.useRef(null)

  React.useEffect(() => {
    inputRef.current.focus()
  }, [])

  return <HStack mt="auto" flex="0 0 auto">
    <FormControl>
      {/* <FormLabel>Capture a thought</FormLabel> */}
      <Textarea
        borderTopRadius="md"
        placeholder="Capture a thought"
        background="background.floor"
        ref={inputRef}
        enterkeyhint="send"
        borderBottomRadius={0}
        value={string}
        onChange={e => setString(e.target.value)}
      />
    </FormControl>
  </HStack>
}

const SavedInput = (props) => {
  const { string, timestamp, isSaved } = props;

  return (<VStack
    borderRadius="md"
    align="stretch" background="background.floor">
    <HStack justifyContent="space-between">
      <Text>{isSaved ? "Saved" : "Waiting to save"}</Text>
      <Text>{timestamp.toLocaleDateString()}</Text>
    </HStack>
    <Text>{string}</Text></VStack>)

}

export const QuickCapture = ({ savedCaptures }) => {
  const [captures, setCaptures] = React.useState(savedCaptures || []);
  const { toolbarHeight } = React.useContext(LayoutContext);

  const onSaveCapture = (string) => {
    setCaptures([...captures, { string, timestamp: new Date() }]);
  }

  return <VStack
    bg="background.basement"
    align="stretch"
    width="100vw"
    height="100svh"
    border="1px solid"
    pt={toolbarHeight}
    overflow="hidden"
  >
    <VStack flex={1}>
      {captures.map((capture, index) => <SavedInput key={capture.timestamp} {...capture} />)}
    </VStack>
    <FloatingInput onSubmit={onSaveCapture} />
  </VStack>
}