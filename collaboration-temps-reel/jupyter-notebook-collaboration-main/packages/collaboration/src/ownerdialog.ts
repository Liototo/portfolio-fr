import { Dialog, showDialog } from '@jupyterlab/apputils';
import { ITranslator, nullTranslator } from '@jupyterlab/translation';

/**
 * Shows a warning message if the current user is the session owner
 * 
 * @param translator the tool to translate the message
 */
export async function showOwnerDialog(translator: ITranslator | null): Promise<Dialog.IResult<string>> {

    const trans = (translator ?? nullTranslator).load('collaboration');

    return showDialog({
        title: trans.__('Owner'),
        body: trans.__('You have been set as the session owner and have been granted extra privileges. Be aware that ' +
        'refreshing or closing the page may make you lose those privileges.'),
        buttons: [
            Dialog.okButton({label: trans.__('OK')})
        ]
    });

}