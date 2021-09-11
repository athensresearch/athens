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
    display: grid;
    justify-items: stretch;
    text-align: left;
    grid-template-areas: "icon name" "icon detail";
    grid-template-columns: auto 1fr;
    gap: 0 0.125rem;

    ${DatabaseIcon.Wrap} {
        grid-area: icon;
        grid-row: 1 / -1;
        margin: auto;
    }
`;

const Name = styled.h3`
    margin: 0;
    display: flex;
    grid-area: name;
    justify-self: stretch;
    font-weight: normal;
    font-size: var(--font-size--text-base);
`;

const Detail = styled.span`
    grid-area: detail;
    max-width: 100%;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    font-size: var(--font-size--text-xs);
    color: var(--body-text-color---opacity-med);
`;

export const Item = ({ onChooseDatabase, db }) => {
    return (
        <ItemWrap>
            <ItemButton onClick={onChooseDatabase}>
                <DatabaseIcon {...db} size="2em" />
                <Name>{db.name}</Name>
                <Detail>{db.id}</Detail>
            </ItemButton>
        </ItemWrap>
    );
};
