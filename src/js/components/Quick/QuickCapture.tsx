import React from 'react'
import { FormControl, HStack, Text, Textarea, VStack } from "@chakra-ui/react"
import { CheckmarkCircleFillIcon } from '@/Icons/Icons'
import { AnimatePresence, motion } from 'framer-motion'

const FloatingInput = (props) => {
  const { onSubmit } = props
  const [string, setString] = React.useState("")
  const inputRef = React.useRef(null)

  const handleSubmit = (e) => {
    if (string.length) {
      e.preventDefault()
      onSubmit(string)
      setString("")
      inputRef.current.focus()
    }
  }

  React.useEffect(() => {
    inputRef.current.focus()
  }, [])

  return <HStack
    mt="auto"
    flex="0 0 auto"
    position="sticky"
    inset={0}
    p={4}
    top="auto"
  >
    <FormControl>
      <Textarea
        ref={inputRef}
        height="20vh"
        borderRadius="lg"
        resize="none"
        placeholder="Tap to begin writing"
        border="1px solid"
        borderColor="separator.divider"
        backgroundClip="border-box"
        background="background.attic"
        _hover={{
          shadow: "page"
        }}
        enterkeyhint="send"
        onKeyDown={(e) => {
          if (e.key === "Enter") {
            handleSubmit(e)
          }
        }}
        value={string}
        shadow="page"
        _focus={{
          shadow: "page"
        }}
        onChange={e => setString(e.target.value)}
      />
    </FormControl>
  </HStack>
}

const Placeholder = () => {
  return <VStack
    as={motion.div}
    initial={{
      opacity: 0,
      y: 50,
    }}
    animate={{
      opacity: 1,
      y: 0,
    }}
    exit={{
      opacity: -2,
      y: -100,
    }}
    mt="auto" color="foreground.secondary">
    <Text fontSize="sm">Save a message to today's Daily Note.</Text>
  </VStack>
}

const SavedItem = (props) => {
  const { string, timestamp, isSaved } = props;
  const itemRef = React.useRef(null)

  React.useLayoutEffect(() => {
    if (itemRef.current) {
      itemRef.current.scrollIntoView({ behavior: "smooth", block: "start" })
    }
  }, [])

  return (<VStack
    _first={{
      marginTop: "auto"
    }}
    flex="0 0 auto"
    ref={itemRef}
    as={motion.div}
    initial={{
      opacity: 0,
      y: 50,
    }}
    animate={{
      opacity: 1,
      y: 0,
    }}
    exit={{
      opacity: 0,
      y: 100,
    }}
    px={4}
    spacing={0}
    align="stretch"
  >
    <VStack
      borderRadius="lg"
      spacing={1}
      align="stretch"
      overflow="hidden"
      background="background.upper"
      px={4}
      py={3}
    >
      <HStack
        fontSize="xs"
        color="foreground.secondary"
        justifyContent="space-between"
      >
        <Text>{timestamp.toLocaleString()}</Text>
        <Text
          display="inline-flex"
          gap={1}
          alignItems="center"
        >
          {isSaved ? (
            <>Saved <CheckmarkCircleFillIcon /></>) : "Waiting to save"}</Text>
      </HStack>
      <Text>{string}</Text></VStack>
  </VStack>);
}

export const QuickCapture = ({ savedCaptures }) => {
  const [captures, setCaptures] = React.useState(savedCaptures || []);
  const containerRef = React.useRef(null)

  const onSaveCapture = (string) => {
    setCaptures([...captures, { string, isSaved: false, timestamp: new Date() }]);
  }

  return <VStack
    align="stretch"
    bg="linear-gradient(to bottom, #00000000 50%, #00000011)"
    backgroundAttachment="fixed"
    pt={4}
    overflow="hidden"
    height="100dvh"
    width="100vw"
    position="relative"
  >
    <style>
      {`
        html, body {
          height: 100%;
          width: 100%;
          position: fixed;
          overflow: hidden;
          margin: 0;
          padding: 0;
        }
      `}

    </style>
    <VStack
      flex={1}
      minHeight="100%"
      align="stretch"
      ref={containerRef}
      overflow="auto"
      overscrollBehaviorY='contain'
    >
      <AnimatePresence initial={true}>
        {captures.length ? captures.map((capture, index) => <SavedItem key={capture.timestamp} {...capture} />)
          : <Placeholder key="placeholder" />}
      </AnimatePresence>
      <FloatingInput onSubmit={onSaveCapture} />
    </VStack>
  </VStack>
}