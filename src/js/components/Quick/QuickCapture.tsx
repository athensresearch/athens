import React from 'react'
import { AlertDialog, AlertDialogBody, AlertDialogContent, AlertDialogFooter, AlertDialogHeader, AlertDialogOverlay, Box, Button, FormControl, HStack, Text, Textarea, VStack } from "@chakra-ui/react"
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
    py={4}
    top="auto"
    align="center"
    justifyContent="center"
  >
    <FormControl
      width="100%"
      flex="0 1 auto"
    >
      <Textarea
        ref={inputRef}
        height="20vh"
        borderRadius="lg"
        resize="none"
        placeholder="Tap to begin writing"
        border="none"
        outline="none"
        shadow="page"
        _focus={{
          outline: "none",
          shadow: "page",
        }}
        background="background.attic"
        enterkeyhint="send"
        onKeyDown={(e) => {
          if (e.key === "Enter") {
            handleSubmit(e)
          }
        }}
        value={string}
        onChange={e => setString(e.target.value)}
      />
    </FormControl>
  </HStack>
}

const QueuedNote = (props) => {
  const { string, timestamp, isSaved } = props;
  const itemRef = React.useRef(null)

  React.useLayoutEffect(() => {
    if (itemRef.current) {
      itemRef.current.scrollIntoView({ behavior: "smooth", block: "start" })
    }
  }, [])

  return (<VStack
    flex="0 0 auto"
    ref={itemRef}
    layout
    as={motion.div}
    initial={{
      opacity: 0,
      height: 0,
      y: "20vh",
    }}
    animate={{
      opacity: 1,
      height: "auto",
      y: 0,
    }}
    exit={{
      opacity: 0,
      height: 0,
      y: -200,
      scale: 0.5,
    }}
    spacing={0}
    alignSelf="stretch"
  >
    <VStack
      borderRadius="lg"
      spacing={1}
      alignSelf="stretch"
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
        <Text>{new Date(timestamp).toLocaleDateString()}</Text>
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

const Message = ({ children, ...props }) => <Text
  layout
  as={motion.p}
  fontSize="sm"
  textAlign="center"
  color="foreground.secondary"
  exit={{
    opacity: 0,
    height: 0,
  }}
  {...props}
>{children}</Text>;

export const QuickCapture = ({ dbMenu, notes, onAddItem, lastSyncTime }) => {
  const [isSwitchDialogOpen, setIsSwitchDialogOpen] = React.useState(false);
  const containerRef = React.useRef(null)
  const confirmationCancelRef = React.useRef(null)

  return <>
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
      align="stretch"
      backgroundAttachment="fixed"
      pt={4}
      overflow="hidden"
      height="100dvh"
      width="100vw"
      position="relative"
      justifyContent="flex-end"
      sx={{
        maskImage: "linear-gradient(to bottom, #00000000 1rem, #000000ff 3rem, #000000ff 100%)"
      }}
    >
      <VStack
        align="stretch"
        maxHeight="100%"
        pt={10}
        px={4}
        bg="linear-gradient(to bottom, #00000000 50%, #00000011)"
        ref={containerRef}
        overflowY="scroll"
        overscrollBehaviorY='contain'
      >
        <AnimatePresence initial={true}>
          {(notes.length) && <Message key="today">Today</Message>}
          {lastSyncTime && <Message key="lastsynced">Last synced: {new Date(lastSyncTime).toLocaleDateString()}</Message>}
          {notes.length && notes.map((note, index) => <QueuedNote key={note.timestamp} {...note} />)}
          {!(notes.length || lastSyncTime) && <Message key="placeholder">Save a message to today's Daily Notes</Message>}
          <FloatingInput onSubmit={onAddItem} />
        </AnimatePresence>
      </VStack>
    </VStack>
    <HStack position="fixed" justifyContent="space-between" inset={3} bottom="auto" as={motion.div}>
      {dbMenu}
      <Button
        borderRadius="full"
        onClick={() => setIsSwitchDialogOpen(true)}
        size="sm"
        sx={{
          backdropFilter: "blur(10px)",
        }}
      >Switch to Athens</Button>
    </HStack>
    <AlertDialog
      size="sm"
      isOpen={isSwitchDialogOpen}
      onClose={() => setIsSwitchDialogOpen(false)}
      leastDestructiveRef={confirmationCancelRef}
    >
      <AlertDialogOverlay />
      <AlertDialogContent py={3}>
        <AlertDialogHeader py={1}>Switch to Athens?</AlertDialogHeader>
        <AlertDialogBody py={1}>
          <Text>Notes that have not been synced will be lost.</Text>
        </AlertDialogBody>
        <AlertDialogFooter py={1}>
          <Button variant="secondary" ref={confirmationCancelRef} onClick={() => setIsSwitchDialogOpen(false)}>Go back</Button>
          <Button variant="secondary" colorScheme="destructive" onClick={() => setIsSwitchDialogOpen(false)}>Switch to Athens</Button>
        </AlertDialogFooter>
      </AlertDialogContent>

    </AlertDialog>
  </>
}