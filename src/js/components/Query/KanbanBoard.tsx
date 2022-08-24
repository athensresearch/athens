import React from 'react';
import { Textarea, VStack, HStack, Grid, Box, Text, Heading, Button, IconButton } from '@chakra-ui/react';
import { Reorder } from 'framer-motion';
import { EditIcon } from '@/Icons/Icons';

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
//     as={Reorder.Item}
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
    /*onClick={() => onClickCard(id)}*/
  >
    <HStack justifyContent="space-between">
        {isEditing
            ? <Textarea ref={textareaRef} value={titleValue} onChange={handleInputChange}
                onBlur={() => {
                    setIsEditing(!isEditing)
                    onUpdateTaskTitle(uid, titleValue)
                }}/>
            : <Text fontWeight={"bold"}>{title}</Text>
            }
        <IconButton icon={<EditIcon/>}
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
      onUpdateStatusClick(uid, columns[columnIndex-1])
      e.stopPropagation()
    }}>←</Button>}
  {(columnIndex < (columnCount - 1)) &&
    <Button value="right" onClick={(e) => {
      e.stopPropagation()
      onUpdateStatusClick(uid, columns[columnIndex+1])
    }
    }>→</Button>}
  </Box>;
}

export const KanbanColumn = (props) => {
  const { name, children, onUpdateKanbanColumn, groupBy } = props;
  // const [items, setItems] = React.useState(["Card 1", "Card 2", "Card 3"]);

  const [isEditing, setIsEditing] = React.useState(false);
  const [titleValue, setTitleValue] = React.useState(name);
  const textareaRef = React.useRef();
  React.useEffect(() => {
    if (textareaRef.current) {
        textareaRef.current.focus()
        const length = textareaRef.current.value.length
        textareaRef.current.selectionStart = length
    }
  }, [isEditing])

  const handleInputChange = (e) => {
     const inputValue = e.target.value
     setTitleValue(inputValue)
   }

  return (
    <Box>
      <VStack
        // as={Reorder.Group}
        align="stretch"
        listStyleType={"none"}
        spacing={2}
        p={2}
        borderRadius="md"
        bg="background.upper"
        axis="y"
        width="300px"
        // values={items}
        // onReorder={setItems}
      >
        <HStack justifyContent="space-between">
            {isEditing
                ? <Textarea ref={textareaRef} value={titleValue} onChange={handleInputChange}
                    onBlur={() => {
                        onUpdateKanbanColumn(groupBy, name, titleValue)
                        setIsEditing(!isEditing)
                    }}/>
                : <Heading color="foreground.secondary" size="sm">{name}</Heading>
                }
            {/* for now, just read-only
            <IconButton icon={<EditIcon/>}
                onClick={(e) => {
                    e.stopPropagation()
                    setIsEditing(!isEditing)
                }} />
            */}
    </HStack>
      {children}
      </VStack>
    </Box>
  );
}

export const KanbanSwimlane = (props) => {
  const { name, children } = props;
  return (
    <VStack align="stretch" borderRadius="md" py={2}>
      <Heading color="foreground.secondary" size="md">{name}</Heading>
      <HStack spacing={5} alignItems="flex-start" overflowX="auto" maxWidth="80vw">{children}</HStack>

      {/*<Grid gap={2} p={0} templateRows="1" templateColumns="repeat(auto-fill, 15rem)" borderRadius="sm">
        {children}
      </Grid>*/}
    </VStack>
  );
}

export const KanbanBoard = (props) => {
  const { name, children } = props;
  return (
    <VStack align="stretch" spacing={3} py={2}>
      <Heading size="md">{name}</Heading>
      {children}
    </VStack>
  );
}

export const ExampleKanban = () => {
  const columns = ["Backlog", "In Progress", "Done"];
  const swimlanes = ["High", "Low"];
  const cards = ["Card 1", "Card 2", "Card 3"];

  return <KanbanBoard name="Task Board">
    {swimlanes.map((swimlane) =>
      <KanbanSwimlane name={swimlane}>
        {columns.map((column) =>
          <KanbanColumn name={column}>
            {}
          </KanbanColumn>
        )}
      </KanbanSwimlane>
    )}
  </KanbanBoard>
}

export const AddCardButton = (props) => {
  const { children, onAddNewCardClick, context } = props
  return <Button size={"sm"} variant={"ghost"} fontWeight={"light"} onClick={() =>
    onAddNewCardClick(context)
    }>
    + New
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

  return (columns && columns.length > 0 &&  <KanbanBoard name={name}>
    {Object.entries(boardData).map(([swimlane, swimlaneColumn]) =>
      <KanbanSwimlane name={swimlane}>
      {columns.map(column =>
        <KanbanColumn name={column.string} onUpdateKanbanColumn={onUpdateKanbanColumn} groupBy={groupBy} >
        {renderCard(columns, column, swimlaneColumn, onUpdateStatusClick, hideProperties, onClickCard, onUpdateTaskTitle)}
        <AddCardButton context={{[groupBy]: column, [subgroupBy]: swimlane}} onAddNewCardClick={onAddNewCardClick} />
        </KanbanColumn>
        )}
      <AddColumnButton onAddNewColumn={onAddNewColumn} />
      </KanbanSwimlane>
      )}
  {/* <AddSwimlaneButton/> */}
  </KanbanBoard>
  )
}


