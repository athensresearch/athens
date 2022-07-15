import React from 'react';
import { Button, Box, Table, Thead, Tbody, Th, Td, Tr } from '@chakra-ui/react';
import { AddCardButton } from '../KanbanBoard/KanbanBoard'
import { ChevronDownIcon, ChevronUpIcon } from '@/Icons/Icons';


const isDateFn = (value) => {
    return (typeof value == "number") && (value > 1000000000000)
}


export const QueryTable = (props) => {
    const { data, columns, dateFormatFn, onClickSort, sortBy, sortDirection, hideProperties } = props;
    return (columns && columns.length > 0 && <Box>
        <Table>
            <Thead>
                <Tr>{columns.map((column) =>
                    (!hideProperties[column] &&
                        <Th key={column} onClick={()=>onClickSort(column)}>
                            <Button>
                                {column}
                                {(sortBy == column &&
                                    (sortDirection == "asc" ? <ChevronUpIcon/> : <ChevronDownIcon/>))}
                            </Button>
                        </Th>))}
                </Tr>
            </Thead>
            <Tbody>
            {data.map((record) => {
                return <Tr>
                {columns.map((column) => {
                    const value = record[column];
                    const isDate = isDateFn(value)
                    return (!hideProperties[column] && <Td>{isDate ? dateFormatFn(value) : value}</Td>)
                })}
                </Tr>})}
            </Tbody>
        </Table>
        <Button display="flex" width="100%" onClick={()=>console.log("TODO: onAddNewCardClick on table view.")}>
            + New
        </Button>
        {/* <AddCardButton column={null} onAddNewCardClick={null} /> */}

    </Box>)
}
