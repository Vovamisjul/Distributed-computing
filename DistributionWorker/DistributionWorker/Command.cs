using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Input;

namespace DistributionWorker
{
    public interface IAsyncCommand : ICommand
    {
        void ExecuteAsync(object parameter);
        bool CanExecute();
    }

    public class DelegateCommand : ICommand
    {
        public event EventHandler CanExecuteChanged;
        
        private readonly Action<object> _execute;
        private readonly Func<object, bool> _canExecute;
        Action<Exception> _onError;

        public DelegateCommand(
            Action<object> execute,
            Func<object, bool> canExecute = null,
            Action<Exception> onError = null)
        {
            _execute = execute;
            _canExecute = canExecute;
            _onError = onError;
        }

        public bool CanExecute(object parameter)
        {
            return _canExecute?.Invoke(parameter) ?? true;
        }

        public void Execute(object parameter)
        {
            try
            {
                _execute?.Invoke(parameter);
            }
            catch (Exception e)
            {
                _onError?.Invoke(e);
            }
        }
    }

    public class AsyncCommand : IAsyncCommand
    {
        public event EventHandler CanExecuteChanged;

        private bool _isExecuting;
        private readonly Func<object, Task> _execute;
        private readonly Func<bool> _canExecute;
        Action<Exception> _onError;

        public AsyncCommand(
            Func<object, Task> execute,
            Func<bool> canExecute = null,
            Action<Exception> onError = null)
        {
            _execute = execute;
            _canExecute = canExecute;
            _onError = onError;
        }

        public bool CanExecute()
        {
            return !_isExecuting && (_canExecute?.Invoke() ?? true);
        }

        public async void ExecuteAsync(object parameter)
        {
            if (CanExecute())
            {
                try
                {
                    _isExecuting = true;
                    await _execute(parameter);
                }
                catch (Exception e)
                {
                    _onError?.Invoke(e);
                }
                finally
                {
                    _isExecuting = false;
                }
            }

            RaiseCanExecuteChanged();
        }

        public void RaiseCanExecuteChanged()
        {
            CanExecuteChanged?.Invoke(this, EventArgs.Empty);
        }

        #region Explicit implementations
        bool ICommand.CanExecute(object parameter)
        {
            return CanExecute();
        }

        void ICommand.Execute(object parameter)
        {
            ExecuteAsync(parameter);
        }
        #endregion
    }
}
