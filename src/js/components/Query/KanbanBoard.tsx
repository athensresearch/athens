import React from 'react';
import { Textarea, VStack, HStack, Grid, Box, Text, Heading, Button, IconButton } from '@chakra-ui/react';
import { Reorder } from 'framer-motion';
import { EditIcon, PlusIcon } from '@/Icons/Icons';

export const KanbanCard = (props) => {
  const { isSelected, columns, onUpdateStatusClick, cardData, hideProperties, onClickCard, onUpdateTaskTitle } = props;
  const title = cardData[":task/title"]
  const status = cardData[":task/status"]
  const uid = cardData[":block/uid"]

  const columnIndex = columns.indexOf(status);
  const columnCount = columns.length;

  const handleInputChange = (e) => {
    const inputValue = e.target.value
    setTitleValue(inputValue)
  }
  const [isEditing, setIsEditing] = React.useState(false);
  const [titleValue, setTitleValue] = React.useState(title);
  const textareaRef = React.useRef();
  React.useEffect(() => {
    if (textareaRef.current) {
      textareaRef.current.focus()
      const length = textareaRef.current.value.length
      textareaRef.current.selectionStart = length
    }
  }, [isEditing])

  return <Box
    key={title}
    value={title}
    borderRadius="sm"
    minHeight="4rem"
    listStyleType={"none"}
    border="1px solid transparent"
    p={2}
    bg="background.floor"
    width="300px"
    _hover={{
      bg: 'background.upper',
      border: "1px solid",
      borderColor: "background.floor"
    }}
  >
    <HStack justifyContent="space-between">
      {isEditing
        ? <Textarea ref={textareaRef} value={titleValue} onChange={handleInputChange}
          onBlur={() => {
            setIsEditing(!isEditing)
            onUpdateTaskTitle(uid, titleValue)
          }} />
        : <Text fontWeight={"bold"}>{title}</Text>
      }
      <IconButton
        aria-role="Edit"
        icon={<EditIcon />}
        onClick={(e) => {
          e.stopPropagation()
          setIsEditing(!isEditing)
        }} />
    </HStack>
    {Object.entries(cardData).map(([key, val]) =>
    (!hideProperties[key]
      && key != ":task/title"
      && <Box display="flex">
        <Text color="gray" width="200px">{key}</Text>
        <Text width="200px">{val}</Text>
      </Box>)
    )}
    {(columnIndex > 0) &&
      <Button value="left" onClick={(e) => {
        onUpdateStatusClick(uid, columns[columnIndex - 1])
        e.stopPropagation()
      }}>←</Button>}
    {(columnIndex < (columnCount - 1)) &&
      <Button value="right" onClick={(e) => {
        e.stopPropagation()
        onUpdateStatusClick(uid, columns[columnIndex + 1])
      }
      }>→</Button>}
  </Box>;
}

export const KanbanColumn = (props) => {
  const { children, isOver } = props;

  const styles = isOver
    ? {
      sx: {
        ":after": {
          content: "''",
          position: "absolute",
          width: "100%",
          height: "3px",
          backgroundColor: "link",
        },
      },
    }
    : null;

  return (
    <VStack
      {...styles}
      flex="1 1 100%"
      position="relative"
      align="stretch"
      listStyleType={"none"}
      spacing={2}
      p={2}
      bg="background.floor"
      borderRadius="md"
      axis="y"
      overflowY="auto"
      maxHeight="100%"
      width="300px"
    >
      {children}
    </VStack>
  );
};

export const KanbanSwimlane = (props) => {
  const { name, children, ...laneProps } = props;
  return (
    <VStack
      align="stretch"
      borderRadius="md"
      spacing={1}
      maxHeight="700px"
      {...laneProps}
    >
      <Heading
        color="foreground.secondary"
        size="md"
        p={2}
        pb={0}
      >
        {name}
      </Heading>
      <HStack
        flex="1 1 100%"
        spacing={4}
        alignItems="stretch"
        justify="stretch"
        p={2}
        overflowY="hidden"
        overflowX="auto"
      >
        {children}
      </HStack>
    </VStack>
  );
}

export const KanbanBoard = (props) => {
  const { children } = props;
  return (
    <VStack
      align="stretch"
      spacing={3}
      py={2}
    >
      {children}
    </VStack>
  );
}

export const ExampleKanban = () => {
  const columns = ["Backlog", "In Progress", "Done"];
  const swimlanes = ["High", "Low"];
  const cards = ["Card 1", "Card 2", "Card 3"];

  return <KanbanBoard>
    {swimlanes.map((swimlane) =>
      <KanbanSwimlane name={swimlane}>
        {columns.map((column) =>
          <KanbanColumn name={column}>
            { }
          </KanbanColumn>
        )}
      </KanbanSwimlane>
    )}
  </KanbanBoard>
}

export const AddCardButton = (props) => {
  const { children, onAddNewCardClick, context } = props
  return <Button
    size={"sm"}
    variant={"ghost"}
    fontWeight={"light"}
    leftIcon={<PlusIcon />}
    onClick={() =>
      onAddNewCardClick(context)
    }>
    Add
  </Button>
};

export const AddColumnButton = (props) => {
  const { children, onAddNewColumn } = props
  return <Button size={"md"} variant={"ghost"} fontWeight={"light"} minWidth={"110"} onClick={onAddNewColumn}>
    + New Column
  </Button>
};

export const AddSwimlaneButton = (props) => {
  const { children, } = props
  return <Button size={"sm"} variant={"ghost"} fontWeight={"light"} onClick={() => console.log("TODO: new swimlane")}>
    + New Swimlane
  </Button>
};

export const renderCard = (columns, column, swimlaneColumn, onUpdateStatusClick, hideProperties, onClickCard, onUpdateTaskTitle) => {
  const key = "((" + column.uid + "))"
  const data = swimlaneColumn[key]
  return data && data.map((cardData) =>
    <KanbanCard
      columns={columns}
      cardData={cardData}
      onUpdateStatusClick={onUpdateStatusClick}
      hideProperties={hideProperties}
      onClickCard={onClickCard}
      onUpdateTaskTitle={onUpdateTaskTitle}
    />
  )
}


export const QueryKanban = (props) => {
  const { boardData, columns, onUpdateStatusClick, onAddNewCardClick, name, hasSubGroup, hideProperties,
    onClickCard, groupBy, subgroupBy, onUpdateTaskTitle, onAddNewColumn, onUpdateKanbanColumn,
    refToString,
  } = props;

  return (columns && columns.length > 0 && <KanbanBoard name={name}>
    {Object.entries(boardData).map(([swimlane, swimlaneColumn]) =>
      <KanbanSwimlane name={swimlane}>
        {columns.map(column =>
          <KanbanColumn name={column.string} onUpdateKanbanColumn={onUpdateKanbanColumn} groupBy={groupBy} >
            {renderCard(columns, column, swimlaneColumn, onUpdateStatusClick, hideProperties, onClickCard, onUpdateTaskTitle)}
            <AddCardButton context={{ [groupBy]: column, [subgroupBy]: swimlane }} onAddNewCardClick={onAddNewCardClick} />
          </KanbanColumn>
        )}
        <AddColumnButton onAddNewColumn={onAddNewColumn} />
      </KanbanSwimlane>
    )}
    {/* <AddSwimlaneButton/> */}
  </KanbanBoard>
  )
}


