import React from 'react';
import { Button, Box, Table, Thead, Tbody, Th, Td, Tr } from '@chakra-ui/react';
import { AddCardButton } from '../KanbanBoard/KanbanBoard'


export const QueryTable = (props) => {
    const { data, columns } = props;
    return (columns && columns.length > 0 && <Box>
        <Table>
            <Thead>
            <Tr>{columns.map((column) =>
                <Th key={column}>{column}</Th>)}</Tr>
            </Thead>
            <Tbody>
            {data.map((props) => {
                return <Tr>
                {columns.map((column) =>
                    <Td>{props[column]}</Td>
                )}
                </Tr>})}
            </Tbody>
        </Table>
        <Button display="flex" width="100%" onClick={()=>console.log("TODO: onAddNewCardClick on table view.")}>
            + New
        </Button>
        {/* <AddCardButton column={null} onAddNewCardClick={null} /> */}

    </Box>)
}
