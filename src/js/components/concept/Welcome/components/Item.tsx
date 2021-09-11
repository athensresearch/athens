import React from 'react';
import styled from 'styled-components';

import { Button } from '../../../Button';
import { DatabaseIcon } from '../../DatabaseIcon'

const ItemWrap = styled.li`
    align-self: stretch;
    display: flex;
    align-items: stretch;
    justify-content: stretch;
`;

const ItemButton = styled(Button)`
    flex-direction: column;
    align-items: stretch;
    gap: 0;
    flex: 1 1 100%;
    overflow: hidden;
`;

const Name = styled.h3`
    margin: 0;
`;

const Detail = styled.span`
    max-width: 100%;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    font-size: var(--font-size--text-xs);
    color: var(--body-text-color---opacity-med);
`;

export const Item = ({ handlePress, name, id, status, isRemote }) => {
    return (
        <ItemWrap>
            <ItemButton onClick={handlePress}>
                <Name>{name}</Name>
                <Detail>{id}</Detail>
            </ItemButton>
        </ItemWrap>
    );
};
