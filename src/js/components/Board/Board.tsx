import React from 'react';
import { Box, Button, Stack, Text, Heading } from '@chakra-ui/react';
import Board from "@asseinfo/react-kanban"

const board = {
  columns: [
    {
      id: 1,
      title: 'Backlog',
      cards: [
        {
          id: 1,
          title: 'Add card',
          description: 'Add capability to add a card in a column'
        },
      ]
    },
    {
      id: 2,
      title: 'Doing',
      cards: [
        {
          id: 2,
          title: 'Drag-n-drop support',
          description: 'Move a card between the columns'
        },
      ]
    }
  ]
}


const ColumnHeader = ({ id, title, description }) => {
  return (
    <Box spacing={4} borderWidth={1} padding={5} margin={5} borderRadius={"lg"} backgroundColor={"gray.200"}>
      <Heading size={"lg"}>{title}</Heading>
  </Box>);
};



const Card = ({ id, title, description }) => {
  return (
    <Box spacing={4} borderWidth={1} padding={3} margin={5} borderRadius={"sm"} backgroundColor={"gray.400"}>
      <Heading size={"md"}>{title}</Heading>
      <Text fontSize={"md"}>{description}</Text>
  </Box>);
};


export const KanbanBoard = ({tasks}) => {
  console.log(tasks)
  return (
    <Board
      initialBoard={board}
      renderCard={Card}
      renderColumnHeader={ColumnHeader}
      />
  );
};
